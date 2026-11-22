package com.innowise.userservice.service;

import com.innowise.userservice.config.EvictAllCaches;
import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.spec.CardSpecification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.innowise.userservice.config.CacheNames.CARDS;
import static com.innowise.userservice.config.CacheNames.CARD_PAGES;
import static com.innowise.userservice.config.CacheNames.USER_CARDS;

@Service
public class PaymentCardService {

    private static final int MAX_ACTIVE_CARDS = 5;

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardService(PaymentCardRepository paymentCardRepository,
                              UserRepository userRepository,
                              PaymentCardMapper paymentCardMapper) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }

    @Transactional
    @EvictAllCaches
    public PaymentCardDto createPaymentCard(Long userId, PaymentCardDto cardDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with " + userId + " not found"));

        boolean willBeActive = cardDto.active() == null || cardDto.active();
        if (willBeActive) {
            checkActiveCardsLimit(userId);
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(cardDto);
        paymentCard.setActive(willBeActive);
        paymentCard.setUser(user);

        PaymentCard savedCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(savedCard);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CARDS, key = "#id")
    public PaymentCardDto findPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card with " + id + " not found"));
        return paymentCardMapper.toDto(paymentCard);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = CARD_PAGES,
            key = "#name + '_' + #surname + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()"
    )
    public Page<PaymentCardDto> getAllPaymentCards(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> spec = Specification.where(CardSpecification.hasName(name))
                .and(CardSpecification.hasSurname(surname));
        return paymentCardRepository.findAll(spec, pageable).map(paymentCardMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = USER_CARDS, key = "#userId")
    public List<PaymentCardDto> findPaymentCardByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId).stream()
                .map(paymentCardMapper::toDto)
                .toList();
    }

    @Transactional
    @EvictAllCaches
    public PaymentCardDto updatePaymentCard(Long id, PaymentCardDto paymentCardDto) {
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        PaymentCard updatedCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card with " + id + " not found"));

        boolean wasActive = Boolean.TRUE.equals(updatedCard.getActive());
        boolean willBeActive = paymentCard.getActive() == null ? wasActive : paymentCard.getActive();

        if (!wasActive && willBeActive) {
            checkActiveCardsLimit(updatedCard.getUser().getId());
        }
        updatedCard.setNumber(paymentCard.getNumber());
        updatedCard.setHolder(paymentCard.getHolder());
        updatedCard.setExpirationDate(paymentCard.getExpirationDate());
        updatedCard.setActive(willBeActive);
        PaymentCard savedCard = paymentCardRepository.save(updatedCard);
        return paymentCardMapper.toDto(savedCard);
    }

    @Transactional
    @EvictAllCaches
    public PaymentCardDto activatePaymentCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card with " + id + " not found"));

        if (!Boolean.TRUE.equals(card.getActive())) {
            checkActiveCardsLimit(card.getUser().getId());
        }
        paymentCardRepository.updateActiveById(id, true);
        return paymentCardMapper.toDto(paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card with " + id + " not found")));
    }

    @Transactional
    @EvictAllCaches
    public PaymentCardDto deactivatePaymentCard(Long id) {
        if (paymentCardRepository.updateActiveById(id, false) == 0) {
            throw new CardNotFoundException("Card with " + id + " not found");
        }
        return paymentCardMapper.toDto(paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card with " + id + " not found")));
    }

    @Transactional
    @EvictAllCaches
    public void deleteCard(Long cardId) {
        PaymentCard paymentCard = paymentCardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with " + cardId + " not found"));
        paymentCardRepository.delete(paymentCard);
    }

    private void checkActiveCardsLimit(Long userId) {
        if (paymentCardRepository.countByUserIdAndActiveTrue(userId) >= MAX_ACTIVE_CARDS) {
            throw new CardLimitExceededException(
                    "Maximum limit of " + MAX_ACTIVE_CARDS + " active payment cards reached");
        }
    }
}
