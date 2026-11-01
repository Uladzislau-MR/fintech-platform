package com.inno.radkevich.userservice.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCardDto(
        @Nullable
        Long id,
        @NotNull
        Long userId,
        @NotBlank
        String number,
        @NotBlank
        String holder,
        @NotBlank
        String expirationDate,
        Boolean active
) {
}
