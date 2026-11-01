package com.inno.radkevich.userservice.exception;

public class NoCardsException extends RuntimeException{
    public NoCardsException(String message) {
        super(message);
    }
}
