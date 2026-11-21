package com.innowise.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;

public record PaymentCardDto(
        @Nullable
        Long id,
        @Nullable
        Long userId,
        @NotBlank
        String number,
        @NotBlank
        String holder,
        @NotBlank
        String expirationDate,
        @NotNull
        Boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;
}