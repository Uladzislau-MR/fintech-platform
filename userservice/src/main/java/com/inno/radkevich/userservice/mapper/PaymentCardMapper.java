package com.inno.radkevich.userservice.mapper;

import com.inno.radkevich.userservice.dto.PaymentCardDto;
import com.inno.radkevich.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentCardMapper {

    @Mapping(target = "userId", source = "user.id")
            PaymentCardDto toDto(PaymentCard paymentCard);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
            PaymentCard toEntity(PaymentCardDto paymentCardDto);
}
