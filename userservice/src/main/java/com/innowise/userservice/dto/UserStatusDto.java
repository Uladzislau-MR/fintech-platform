package com.innowise.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusDto(@NotNull Boolean active) {
}