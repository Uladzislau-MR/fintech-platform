package com.inno.radkevich.userservice.service;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.entity.PaymentCard;
import com.inno.radkevich.userservice.entity.User;
import com.inno.radkevich.userservice.exception.CardNotFoundException;
import com.inno.radkevich.userservice.exception.UserNotFoundException;
import com.inno.radkevich.userservice.mapper.PaymentCardMapper;
import com.inno.radkevich.userservice.repository.PaymentCardRepository;
import com.inno.radkevich.userservice.repository.UserRepository;
import com.inno.radkevich.userservice.spec.CardSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = "paymentCards", allEntries = true)
    public PaymentCardDto createPaymentCard(PaymentCardDto paymentCardDto, Long userId) {
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        paymentCard.setUser(user);
        user.addCard(paymentCard);
        userRepository.save(user);
        return paymentCardMapper.toDto(paymentCard);
    }

    @Cacheable(value = "paymentCards", key = "#id")
    public PaymentCardDto findPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("card with" + id + "not found"));
        return paymentCardMapper.toDto(paymentCard);
    }


    @Cacheable(value = "paymentCards", key = "#name + '_' + #surname + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PaymentCardDto> getAllPaymentCards(String name, String surname, Pageable pageable) {
        Specification<PaymentCard> spec = Specification.where(CardSpecification.hasName(name).and(CardSpecification.hasSurname(surname)));
        Page<PaymentCard> paymentCards = paymentCardRepository.findAll(spec, pageable);
        return paymentCards.map(card -> paymentCardMapper.toDto(card));
    }

    @Cacheable(value = "paymentCards", key = "#id")
    public List<PaymentCardDto> findPaymentCardByUserId(Long id) {
        List<PaymentCard> paymentCardDtoList = paymentCardRepository.findByUserId(id);
        return paymentCardDtoList.stream().map(card -> paymentCardMapper.toDto(card)).toList();
    }

    @Transactional
    @CacheEvict(value = "paymentCards", allEntries = true)
    public PaymentCardDto updatePaymentCard(Long id, PaymentCardDto paymentCardDto) {
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        PaymentCard updatedCard = paymentCardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("card with" + id + "not found"));
        updatedCard.setUser(paymentCard.getUser());
        updatedCard.setNumber(paymentCard.getNumber());
        updatedCard.setHolder(paymentCard.getHolder());
        updatedCard.setExpirationDate(paymentCard.getExpirationDate());
        updatedCard.setActive(paymentCard.getActive());
        return paymentCardMapper.toDto(updatedCard);
    }

    @Transactional
    @CacheEvict(value = "paymentCards", allEntries = true)
    public PaymentCardDto activatePaymentCard(Long id) {

        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("card with" + id + "not found"));
        card.setActive(true);
        return paymentCardMapper.toDto(card);

    }

    @Transactional
    @CacheEvict(value = "paymentCards", allEntries = true)
    public PaymentCardDto deactivatePaymentCard(Long id) {

        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("card with" + id + "not found"));
        card.setActive(false);
        return paymentCardMapper.toDto(card);

    }

}
