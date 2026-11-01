package com.inno.radkevich.userservice.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.io.Serializable;
import java.time.LocalDate;

public record UserDto(
        @Nullable
        Long id,
        @NotBlank
        String name,
        @NotBlank
        String surname,
        @NotNull
        LocalDate birthDate,
        @Email
        String email,
        Boolean active
) implements Serializable {
    private static final long serialVersionUID = 1L;
}