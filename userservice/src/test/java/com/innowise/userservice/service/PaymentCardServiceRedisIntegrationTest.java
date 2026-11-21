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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PaymentCardServiceRedisIntegrationTest extends AbstractIntegrationTest {

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
                cache.clear();
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

        testCard = paymentCardService.createPaymentCard(testUser.id(), new PaymentCardDto(
                null,
                testUser.id(),
                "4000123456789010",
                "Shura Balaganov",
                "12/28",
                true
        ));
    }

    @Test
    void deleteUser_evictsCardCache(){
        paymentCardService.findPaymentCardById(testCard.id());
        userService.deleteUser(testUser.id());
         assertThrows(CardNotFoundException.class, () -> paymentCardService.findPaymentCardById(testCard.id()));

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
    void cardLimitExceededTest() {
        for (int i = 1; i <= 4; i++) {
            paymentCardService.createPaymentCard(testUser.id(), new PaymentCardDto(
                    null,
                    testUser.id(),
                    "400012345678901" + i,
                    "Shura Balaganov",
                    "12/28",
                    true
            ));
        }

        PaymentCardDto sixthCard = new PaymentCardDto(
                null,
                testUser.id(),
                "4000123456789099",
                "Shura Balaganov",
                "12/28",
                true
        );

        assertThrows(
                CardLimitExceededException.class,
                () -> paymentCardService.createPaymentCard(testUser.id(), sixthCard)
        );
    }

    @Test
    void updateCardEvictCacheTest() {
        PaymentCardDto cached = paymentCardService.findPaymentCardById(testCard.id());
        assertEquals("4000123456789010", cached.number());

        PaymentCardDto updatedDto = new PaymentCardDto(
                testCard.id(),
                testUser.id(),
                "4000123456789999",
                "Shura Balaganov",
                "12/29",
                true
        );
        paymentCardService.updatePaymentCard(testCard.id(), updatedDto);

        PaymentCardDto afterUpdate = paymentCardService.findPaymentCardById(testCard.id());
        assertEquals("4000123456789999", afterUpdate.number());
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