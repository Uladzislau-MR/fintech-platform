package com.innowise.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.LocalDate;

public record PaymentCardDto(
        @Nullable
        Long id,
        @Nullable
        Long userId,
        @NotBlank
        String number,
        @NotBlank
        String holder,
        @NotNull
        @Future
        LocalDate expirationDate,
        Boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;
}