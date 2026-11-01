package com.inno.radkevich.userservice.mapper;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentCardMapper {
            PaymentCardDto toDto(PaymentCard paymentCard);
            PaymentCard toEntity(PaymentCardDto paymentCardDto);
}
