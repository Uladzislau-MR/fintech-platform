package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    private static final LocalDate EXPIRATION = LocalDate.of(2030, 12, 31);

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private Long userId;
    private Long cardId;
    private User mockUser;
    private PaymentCard mockCard;
    private PaymentCardDto expectedDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        cardId = 10L;

        mockUser = new User(
                userId,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true
        );

        mockCard = new PaymentCard(
                cardId,
                mockUser,
                "4000123456789010",
                "Shura Balaganov",
                EXPIRATION,
                true
        );

        expectedDto = new PaymentCardDto(
                cardId,
                userId,
                "4000123456789010",
                "Shura Balaganov",
                EXPIRATION,
                true
        );
    }

    @Test
    void createPaymentCardTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(2L);
        when(paymentCardMapper.toEntity(expectedDto)).thenReturn(mockCard);
        when(paymentCardRepository.save(mockCard)).thenReturn(mockCard);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.createPaymentCard(userId, expectedDto);

        assertEquals(expectedDto, actual);
        assertEquals(mockUser, mockCard.getUser());
        verify(paymentCardRepository).save(mockCard);
        verify(paymentCardMapper).toDto(mockCard);
    }

    @Test
    void createPaymentCardDefaultsActiveToTrueAndChecksLimitTest() {
        PaymentCardDto withoutActive = new PaymentCardDto(
                null, null, "4000123456789010", "Shura Balaganov", EXPIRATION, null);
        PaymentCard entity = new PaymentCard(null, null, "4000123456789010", "Shura Balaganov", EXPIRATION, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(1L);
        when(paymentCardMapper.toEntity(withoutActive)).thenReturn(entity);
        when(paymentCardRepository.save(entity)).thenReturn(entity);
        when(paymentCardMapper.toDto(entity)).thenReturn(expectedDto);

        paymentCardService.createPaymentCard(userId, withoutActive);

        assertTrue(entity.getActive());
        verify(paymentCardRepository).countByUserIdAndActiveTrue(userId);
    }

    @Test
    void createInactivePaymentCardSkipsLimitCheckTest() {
        PaymentCardDto inactiveDto = new PaymentCardDto(
                null, null, "4000123456789010", "Shura Balaganov", EXPIRATION, false);
        PaymentCard entity = new PaymentCard(null, null, "4000123456789010", "Shura Balaganov", EXPIRATION, false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardMapper.toEntity(inactiveDto)).thenReturn(entity);
        when(paymentCardRepository.save(entity)).thenReturn(entity);
        when(paymentCardMapper.toDto(entity)).thenReturn(expectedDto);

        paymentCardService.createPaymentCard(userId, inactiveDto);

        assertFalse(entity.getActive());
        verify(paymentCardRepository, never()).countByUserIdAndActiveTrue(anyLong());
    }

    @Test
    void createPaymentCardUserNotFoundTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> paymentCardService.createPaymentCard(userId, expectedDto));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void createPaymentCardLimitExceededTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.createPaymentCard(userId, expectedDto));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void findPaymentCardByIdTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.findPaymentCardById(cardId);

        assertEquals(expectedDto, actual);
        verify(paymentCardRepository).findById(cardId);
    }

    @Test
    void findPaymentCardByIdNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.findPaymentCardById(cardId));
    }

    @Test
    void getAllPaymentCardsTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> page = new PageImpl<>(List.of(mockCard), pageable, 1);

        when(paymentCardRepository.findAll(ArgumentMatchers.<Specification<PaymentCard>>any(), eq(pageable)))
                .thenReturn(page);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        Page<PaymentCardDto> actual = paymentCardService.getAllPaymentCards("Shura", "Balaganov", pageable);

        assertEquals(1, actual.getTotalElements());
        assertEquals(expectedDto, actual.getContent().getFirst());
    }

    @Test
    void findPaymentCardByUserIdTest() {
        when(paymentCardRepository.findByUserId(userId)).thenReturn(List.of(mockCard));
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        List<PaymentCardDto> actual = paymentCardService.findPaymentCardByUserId(userId);

        assertEquals(List.of(expectedDto), actual);
        verify(paymentCardRepository).findByUserId(userId);
    }

    @Test
    void updatePaymentCardTest() {
        mockCard.setActive(false);
        PaymentCard mappedCard = new PaymentCard(
                null, null, "4000123456789099", "Shura B.", LocalDate.of(2031, 1, 31), true);

        when(paymentCardMapper.toEntity(expectedDto)).thenReturn(mappedCard);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(4L);
        when(paymentCardRepository.save(mockCard)).thenReturn(mockCard);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.updatePaymentCard(cardId, expectedDto);

        assertEquals(expectedDto, actual);
        assertEquals(mockUser, mockCard.getUser(), "owner must not change on update");
        assertEquals("4000123456789099", mockCard.getNumber());
        assertEquals("Shura B.", mockCard.getHolder());
        assertEquals(LocalDate.of(2031, 1, 31), mockCard.getExpirationDate());
        assertTrue(mockCard.getActive());
    }

    @Test
    void updatePaymentCardKeepsActiveWhenNotProvidedTest() {
        PaymentCard mappedCard = new PaymentCard(
                null, null, "4000123456789099", "Shura B.", EXPIRATION, null);

        when(paymentCardMapper.toEntity(expectedDto)).thenReturn(mappedCard);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.save(mockCard)).thenReturn(mockCard);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        paymentCardService.updatePaymentCard(cardId, expectedDto);

        assertTrue(mockCard.getActive());
        verify(paymentCardRepository, never()).countByUserIdAndActiveTrue(anyLong());
    }

    @Test
    void updatePaymentCardLimitExceededTest() {
        mockCard.setActive(false);
        PaymentCard mappedCard = new PaymentCard(
                null, null, "4000123456789010", "Shura Balaganov", EXPIRATION, true);

        when(paymentCardMapper.toEntity(expectedDto)).thenReturn(mappedCard);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.updatePaymentCard(cardId, expectedDto));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void updatePaymentCardNotFoundTest() {
        when(paymentCardMapper.toEntity(expectedDto)).thenReturn(mockCard);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.updatePaymentCard(cardId, expectedDto));
    }

    @Test
    void activatePaymentCardTest() {
        mockCard.setActive(false);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(4L);
        when(paymentCardRepository.updateActiveById(cardId, true)).thenReturn(1);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.activatePaymentCard(cardId);

        assertEquals(expectedDto, actual);
        verify(paymentCardRepository).updateActiveById(cardId, true);
    }

    @Test
    void activateAlreadyActiveCardSkipsLimitCheckTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.updateActiveById(cardId, true)).thenReturn(1);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        paymentCardService.activatePaymentCard(cardId);

        verify(paymentCardRepository, never()).countByUserIdAndActiveTrue(anyLong());
    }

    @Test
    void activatePaymentCardLimitExceededTest() {
        mockCard.setActive(false);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.activatePaymentCard(cardId));
        verify(paymentCardRepository, never()).updateActiveById(anyLong(), ArgumentMatchers.anyBoolean());
    }

    @Test
    void activatePaymentCardNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.activatePaymentCard(cardId));
    }

    @Test
    void deactivatePaymentCardTest() {
        when(paymentCardRepository.updateActiveById(cardId, false)).thenReturn(1);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.deactivatePaymentCard(cardId);

        assertEquals(expectedDto, actual);
        verify(paymentCardRepository).updateActiveById(cardId, false);
    }

    @Test
    void deactivatePaymentCardNotFoundTest() {
        when(paymentCardRepository.updateActiveById(cardId, false)).thenReturn(0);

        assertThrows(CardNotFoundException.class, () -> paymentCardService.deactivatePaymentCard(cardId));
        verify(paymentCardRepository, never()).findById(cardId);
    }

    @Test
    void deleteCardTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        paymentCardService.deleteCard(cardId);

        verify(paymentCardRepository).delete(mockCard);
    }

    @Test
    void deleteCardNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.deleteCard(cardId));
        verify(paymentCardRepository, never()).delete(any(PaymentCard.class));
    }
}
