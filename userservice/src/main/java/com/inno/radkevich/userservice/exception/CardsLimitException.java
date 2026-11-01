package com.inno.radkevich.userservice.exception;

public class CardsLimitException extends RuntimeException{
    public CardsLimitException(String message) {
        super(message);
    }
}
