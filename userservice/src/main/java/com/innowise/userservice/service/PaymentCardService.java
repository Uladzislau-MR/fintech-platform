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
import com.innowise.userservice.spec.CardSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentCardService {
    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    public PaymentCardService(PaymentCardRepository paymentCardRepository, UserRepository userRepository, PaymentCardMapper paymentCardMapper) {
        this.paymentCardRepository = paymentCardRepository;
        this.userRepository = userRepository;
        this.paymentCardMapper = paymentCardMapper;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCards", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    public PaymentCardDto createPaymentCard(Long userId, PaymentCardDto cardDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (Boolean.TRUE.equals(cardDto.active())) {
            long activeCount = paymentCardRepository.countByUserIdAndActiveTrue(userId);
            if (activeCount >= 5) {
                throw new CardLimitExceededException("Maximum limit of 5 active payment cards reached");
            }
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(cardDto);
        paymentCard.setUser(user);

        PaymentCard savedCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(savedCard);
    }

    @Cacheable(value = "paymentCards", key = "#id")
    public PaymentCardDto findPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("card with " + id + " not found"));
        return paymentCardMapper.toDto(paymentCard);
    }

    @Cacheable(value = "paymentCards", key = "#name + '_' + #surname + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<PaymentCardDto> getAllPaymentCards(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> spec = Specification.where(CardSpecification.hasName(name).and(CardSpecification.hasSurname(surname)));
        Page<PaymentCard> paymentCards = paymentCardRepository.findAll(spec, pageable);
        return paymentCards.map(card -> paymentCardMapper.toDto(card));
    }

    @Cacheable(value = "paymentCards", key = "'user_cards_' + #id")
    public List<PaymentCardDto> findPaymentCardByUserId(Long id) {
        List<PaymentCard> paymentCardDtoList = paymentCardRepository.findByUserId(id);
        return paymentCardDtoList.stream().map(card -> paymentCardMapper.toDto(card)).toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCards", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    public PaymentCardDto updatePaymentCard(Long id, PaymentCardDto paymentCardDto) {
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        PaymentCard updatedCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("card with " + id + " not found"));

        boolean wasActive = Boolean.TRUE.equals(updatedCard.getActive());
        boolean willBeActive = Boolean.TRUE.equals(paymentCard.getActive());

        if (!wasActive && willBeActive) {
            long activeCount = paymentCardRepository.countByUserIdAndActiveTrue(updatedCard.getUser().getId());
            if (activeCount >= 5) {
                throw new CardLimitExceededException("Maximum limit of 5 active payment cards reached");
            }
        }
        updatedCard.setNumber(paymentCard.getNumber());
        updatedCard.setHolder(paymentCard.getHolder());
        updatedCard.setExpirationDate(paymentCard.getExpirationDate());
        updatedCard.setActive(paymentCard.getActive());
        PaymentCard savedCard = paymentCardRepository.save(updatedCard);
        return paymentCardMapper.toDto(savedCard);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCards", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    public PaymentCardDto activatePaymentCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("card with " + id + " not found"));

        long activeCount = paymentCardRepository.countByUserIdAndActiveTrue(card.getUser().getId());
        if (activeCount >= 5) {
            throw new CardLimitExceededException("Maximum limit of 5 active payment cards reached");
        }
        card.setActive(true);
        PaymentCard savedCard = paymentCardRepository.save(card);
        return paymentCardMapper.toDto(savedCard);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCards", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    public PaymentCardDto deactivatePaymentCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("card with " + id + " not found"));
        card.setActive(false);
        PaymentCard savedCard = paymentCardRepository.save(card);
        return paymentCardMapper.toDto(savedCard);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "paymentCards", allEntries = true),
            @CacheEvict(value = "users", allEntries = true)
    })
    public void deleteCard(Long cardId) {
        PaymentCard paymentCard = paymentCardRepository.findById(cardId).orElseThrow(
                () -> new CardNotFoundException("card with " + cardId + " not found"));
        paymentCardRepository.delete(paymentCard);
    }
}