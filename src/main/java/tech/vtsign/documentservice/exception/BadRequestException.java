package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 400
public class BadRequestException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.BAD_REQUEST;

    public BadRequestException(String message) {
        super(message);
    }
}
