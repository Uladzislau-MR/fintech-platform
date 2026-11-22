package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.dto.UserDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PaymentCardServiceRedisIntegrationTest extends AbstractIntegrationTest {

    private static final LocalDate EXPIRATION = LocalDate.now().plusYears(3);

    @Autowired
    private PaymentCardService paymentCardService;

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private UserDto testUser;
    private PaymentCardDto testCard;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.invalidate();
            }
        });
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userService.createUser(new UserDto(
                null,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura@goldencalf.ru",
                true,
                List.of()
        ));

        testCard = paymentCardService.createPaymentCard(testUser.id(), card("4000123456789010", true));
    }

    private PaymentCardDto card(String number, Boolean active) {
        return new PaymentCardDto(null, null, number, "Shura Balaganov", EXPIRATION, active);
    }

    @Test
    void createCardReturnsIdAndOwnerTest() {
        assertNotNull(testCard.id());
        assertEquals(testUser.id(), testCard.userId());
        assertEquals("4000123456789010", testCard.number());
        assertEquals(EXPIRATION, testCard.expirationDate());
    }

    @Test
    void deleteUserEvictsCardCachesTest() {
        paymentCardService.findPaymentCardById(testCard.id());
        paymentCardService.findPaymentCardByUserId(testUser.id());

        userService.deleteUser(testUser.id());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.findPaymentCardById(testCard.id()));
        assertTrue(paymentCardService.findPaymentCardByUserId(testUser.id()).isEmpty());
    }

    @Test
    void findCardByIdCacheTest() {
        PaymentCardDto firstRead = paymentCardService.findPaymentCardById(testCard.id());
        PaymentCardDto secondRead = paymentCardService.findPaymentCardById(testCard.id());

        assertNotNull(firstRead);
        assertEquals(firstRead, secondRead);
        verify(paymentCardRepository, times(1)).findById(testCard.id());
    }

    @Test
    void cardAndUserCardsCachesDoNotCollideTest() {
        Long sameKey = testCard.id();

        paymentCardService.findPaymentCardById(sameKey);

        assertDoesNotThrow(() -> paymentCardService.findPaymentCardByUserId(sameKey));
        assertDoesNotThrow(() -> paymentCardService.findPaymentCardById(sameKey));
    }

    @Test
    void getAllCardsCacheTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCardDto> firstRead = paymentCardService.getAllPaymentCards("Shura", "Balaganov", pageable);
        Page<PaymentCardDto> secondRead = paymentCardService.getAllPaymentCards("Shura", "Balaganov", pageable);

        assertEquals(1, firstRead.getTotalElements());
        assertEquals(firstRead.getContent(), secondRead.getContent());
        verify(paymentCardRepository, times(1)).findAll(
                org.mockito.ArgumentMatchers.<Specification<PaymentCard>>any(),
                eq(pageable)
        );
    }

    @Test
    void addingCardEvictsCachedUserTest() {
        UserDto before = userService.findById(testUser.id());
        assertEquals(1, before.cards().size());

        paymentCardService.createPaymentCard(testUser.id(), card("4000123456789011", true));

        UserDto after = userService.findById(testUser.id());
        assertEquals(2, after.cards().size());
    }

    @Test
    void deletingCardEvictsCachedUserTest() {
        assertEquals(1, userService.findById(testUser.id()).cards().size());

        paymentCardService.deleteCard(testCard.id());

        assertTrue(userService.findById(testUser.id()).cards().isEmpty());
    }

    @Test
    void sixthActiveCardIsRejectedTest() {
        for (int i = 1; i <= 4; i++) {
            paymentCardService.createPaymentCard(testUser.id(), card("400012345678901" + i, true));
        }

        assertThrows(
                CardLimitExceededException.class,
                () -> paymentCardService.createPaymentCard(testUser.id(), card("4000123456789099", true))
        );
    }

    @Test
    void inactiveCardsDoNotCountTowardsLimitTest() {
        for (int i = 1; i <= 4; i++) {
            paymentCardService.createPaymentCard(testUser.id(), card("400012345678901" + i, true));
        }
        // 5 active cards now; any number of inactive ones is still allowed
        assertDoesNotThrow(() ->
                paymentCardService.createPaymentCard(testUser.id(), card("4000123456789098", false)));
        assertEquals(6, paymentCardService.findPaymentCardByUserId(testUser.id()).size());
    }

    @Test
    void activatingSixthCardIsRejectedTest() {
        for (int i = 1; i <= 4; i++) {
            paymentCardService.createPaymentCard(testUser.id(), card("400012345678901" + i, true));
        }
        PaymentCardDto inactive = paymentCardService.createPaymentCard(testUser.id(), card("4000123456789098", false));

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.activatePaymentCard(inactive.id()));
    }

    @Test
    void activateAndDeactivateCardTest() {
        PaymentCardDto deactivated = paymentCardService.deactivatePaymentCard(testCard.id());
        assertEquals(false, deactivated.active());
        assertEquals(false, paymentCardService.findPaymentCardById(testCard.id()).active());

        PaymentCardDto activated = paymentCardService.activatePaymentCard(testCard.id());
        assertEquals(true, activated.active());
        assertEquals(true, paymentCardService.findPaymentCardById(testCard.id()).active());
    }

    @Test
    void updateCardEvictCacheAndKeepsOwnerTest() {
        PaymentCardDto cached = paymentCardService.findPaymentCardById(testCard.id());
        assertEquals("4000123456789010", cached.number());

        PaymentCardDto updatedDto = new PaymentCardDto(
                testCard.id(), null, "4000123456789999", "Shura Balaganov", EXPIRATION.plusYears(1), true);
        paymentCardService.updatePaymentCard(testCard.id(), updatedDto);

        PaymentCardDto afterUpdate = paymentCardService.findPaymentCardById(testCard.id());
        assertEquals("4000123456789999", afterUpdate.number());
        assertEquals(testUser.id(), afterUpdate.userId());
    }

    @Test
    void deleteCardEvictCacheTest() {
        paymentCardService.findPaymentCardById(testCard.id());
        paymentCardService.deleteCard(testCard.id());

        assertThrows(
                CardNotFoundException.class,
                () -> paymentCardService.findPaymentCardById(testCard.id())
        );
    }
}
