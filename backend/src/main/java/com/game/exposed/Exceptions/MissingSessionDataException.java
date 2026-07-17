package com.game.exposed.Exceptions;

public class MissingSessionDataException extends RuntimeException {
    public MissingSessionDataException(String message) {
        super(message);
    }

    public MissingSessionDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
