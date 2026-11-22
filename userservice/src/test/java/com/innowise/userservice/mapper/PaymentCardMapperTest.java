package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentCardMapperTest {

    private static final LocalDate EXPIRATION = LocalDate.of(2030, 12, 31);

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
                EXPIRATION,
                true
        );

        mockPaymentCardDto = new PaymentCardDto(
                10L,
                1L,
                "4000123456789010",
                "Shura Balaganov",
                EXPIRATION,
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
        assertNull(entity.getId(), "id must be ignored on create");
        assertNull(entity.getUser(), "owner is set by the service, not by the mapper");
        assertEquals(mockPaymentCardDto.number(), entity.getNumber());
        assertEquals(mockPaymentCardDto.holder(), entity.getHolder());
        assertEquals(mockPaymentCardDto.expirationDate(), entity.getExpirationDate());
        assertEquals(mockPaymentCardDto.active(), entity.getActive());
    }
}
