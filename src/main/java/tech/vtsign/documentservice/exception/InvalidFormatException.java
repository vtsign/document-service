package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 422
public class InvalidFormatException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

    public InvalidFormatException(String message) {
        super(message);
    }
}
