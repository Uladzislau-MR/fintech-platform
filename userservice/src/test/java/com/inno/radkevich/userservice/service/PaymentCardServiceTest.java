package com.inno.radkevich.userservice.service;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.entity.PaymentCard;
import com.inno.radkevich.userservice.entity.User;
import com.inno.radkevich.userservice.exception.CardLimitExceededException;
import com.inno.radkevich.userservice.exception.CardNotFoundException;
import com.inno.radkevich.userservice.exception.UserNotFoundException;
import com.inno.radkevich.userservice.mapper.PaymentCardMapper;
import com.inno.radkevich.userservice.repository.PaymentCardRepository;
import com.inno.radkevich.userservice.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceTest {

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
                "12/28",
                true
        );

        expectedDto = new PaymentCardDto(
                cardId,
                userId,
                "4000123456789010",
                "Shura Balaganov",
                "12/28",
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
        verify(userRepository).findById(userId);
        verify(paymentCardRepository).countByUserIdAndActiveTrue(userId);
        verify(paymentCardRepository).save(mockCard);
        verify(paymentCardMapper).toDto(mockCard);
    }

    @Test
    void createPaymentCardUserNotFoundTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> paymentCardService.createPaymentCard(userId, expectedDto));
        verify(userRepository).findById(userId);
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void createPaymentCardLimitExceededTest() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.createPaymentCard(userId, expectedDto));
        verify(paymentCardRepository).countByUserIdAndActiveTrue(userId);
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void findPaymentCardByIdTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.findPaymentCardById(cardId);

        assertEquals(expectedDto, actual);
        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardMapper).toDto(mockCard);
    }

    @Test
    void findPaymentCardByIdNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.findPaymentCardById(cardId));
        verify(paymentCardRepository).findById(cardId);
    }

    @Test
    void getAllPaymentCardsTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> page = new PageImpl<>(List.of(mockCard), pageable, 1);

        when(paymentCardRepository.findAll(ArgumentMatchers.<Specification<PaymentCard>>any(), eq(pageable))).thenReturn(page);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        Page<PaymentCardDto> actual = paymentCardService.getAllPaymentCards("Shura", "Balaganov", pageable);

        assertEquals(1, actual.getTotalElements());
        assertEquals(expectedDto, actual.getContent().getFirst());
        verify(paymentCardRepository).findAll(ArgumentMatchers.<Specification<PaymentCard>>any(), eq(pageable));
        verify(paymentCardMapper).toDto(mockCard);
    }

    @Test
    void findPaymentCardByUserIdTest() {
        when(paymentCardRepository.findByUserId(userId)).thenReturn(List.of(mockCard));
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        List<PaymentCardDto> actual = paymentCardService.findPaymentCardByUserId(userId);

        assertEquals(List.of(expectedDto), actual);
        verify(paymentCardRepository).findByUserId(userId);
        verify(paymentCardMapper).toDto(mockCard);
    }

    @Test
    void updatePaymentCardTest() {
        mockCard.setActive(false);
        PaymentCard mappedCard = new PaymentCard(
                cardId,
                null,
                "4000123456789099",
                "Shura Balaganov",
                "12/29",
                true
        );

        when(paymentCardMapper.toEntity(expectedDto)).thenReturn(mappedCard);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(4L);
        when(paymentCardRepository.save(mockCard)).thenReturn(mockCard);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.updatePaymentCard(cardId, expectedDto);

        assertEquals(expectedDto, actual);
        assertEquals(mockUser, mockCard.getUser());
        assertEquals("4000123456789099", mockCard.getNumber());
        assertTrue(mockCard.getActive());
        verify(paymentCardRepository).save(mockCard);
    }

    @Test
    void updatePaymentCardLimitExceededTest() {
        mockCard.setActive(false);
        PaymentCard mappedCard = new PaymentCard(
                cardId,
                null,
                "4000123456789010",
                "Shura Balaganov",
                "12/28",
                true
        );

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
        verify(paymentCardRepository).findById(cardId);
    }

    @Test
    void activatePaymentCardTest() {
        mockCard.setActive(false);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(4L);
        when(paymentCardRepository.save(mockCard)).thenReturn(mockCard);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.activatePaymentCard(cardId);

        assertEquals(expectedDto, actual);
        assertTrue(mockCard.getActive());
        verify(paymentCardRepository).save(mockCard);
    }

    @Test
    void activatePaymentCardLimitExceededTest() {
        mockCard.setActive(false);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.countByUserIdAndActiveTrue(userId)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class, () -> paymentCardService.activatePaymentCard(cardId));
        verify(paymentCardRepository, never()).save(any(PaymentCard.class));
    }

    @Test
    void activatePaymentCardNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.activatePaymentCard(cardId));
    }

    @Test
    void deactivatePaymentCardTest() {
        mockCard.setActive(true);
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));
        when(paymentCardRepository.save(mockCard)).thenReturn(mockCard);
        when(paymentCardMapper.toDto(mockCard)).thenReturn(expectedDto);

        PaymentCardDto actual = paymentCardService.deactivatePaymentCard(cardId);

        assertEquals(expectedDto, actual);
        assertFalse(mockCard.getActive());
        verify(paymentCardRepository).save(mockCard);
    }

    @Test
    void deactivatePaymentCardNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.deactivatePaymentCard(cardId));
    }

    @Test
    void deleteCardTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(mockCard));

        paymentCardService.deleteCard(cardId);

        verify(paymentCardRepository).findById(cardId);
        verify(paymentCardRepository).delete(mockCard);
    }

    @Test
    void deleteCardNotFoundTest() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> paymentCardService.deleteCard(cardId));
        verify(paymentCardRepository, never()).delete(any(PaymentCard.class));
    }
}