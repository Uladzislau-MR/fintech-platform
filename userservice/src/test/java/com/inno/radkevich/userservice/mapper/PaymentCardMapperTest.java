package com.inno.radkevich.userservice.mapper;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.entity.PaymentCard;
import com.inno.radkevich.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class PaymentCardMapperTest {


    private PaymentCardMapper paymentCardMapper;

    private PaymentCard mockPaymentCard;
    private PaymentCardDto mockPaymentCardDto;

    @BeforeEach
    void setUp() {
        paymentCardMapper = Mappers.getMapper(PaymentCardMapper.class);
        User mockUser = new User(
                1L,
                "Shura",
                "Balaganov",
                LocalDate.of(1902, 5, 15),
                "shura.malochnyi@goldencalf.ru",
                true
        );

        mockPaymentCard = new PaymentCard(
                10L,
                mockUser,
                "4000123456789010",
                "Shura Balaganov",
                "12/28",
                true
        );

        mockPaymentCardDto = new PaymentCardDto(
                10L,
                1L,
                "4000123456789010",
                "Shura Balaganov",
                "12/28",
                true
        );
    }

    @Test
    void paymentCardToDto() {
        PaymentCardDto dto = paymentCardMapper.toDto(mockPaymentCard);

        assertNotNull(dto);
        assertEquals(mockPaymentCard.getId(), dto.id());
        assertEquals(mockPaymentCard.getUser().getId(), dto.userId());
        assertEquals(mockPaymentCard.getNumber(), dto.number());
        assertEquals(mockPaymentCard.getHolder(), dto.holder());
        assertEquals(mockPaymentCard.getExpirationDate(), dto.expirationDate());
        assertEquals(mockPaymentCard.getActive(), dto.active());
    }

    @Test
    void paymentCardDtoToEntity() {
        PaymentCard entity = paymentCardMapper.toEntity(mockPaymentCardDto);

        assertNotNull(entity);
        assertEquals(mockPaymentCardDto.id(), entity.getId());
        assertEquals(mockPaymentCardDto.number(), entity.getNumber());
        assertEquals(mockPaymentCardDto.holder(), entity.getHolder());
        assertEquals(mockPaymentCardDto.expirationDate(), entity.getExpirationDate());
        assertEquals(mockPaymentCardDto.active(), entity.getActive());

    }
}