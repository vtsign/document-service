package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.NOT_FOUND;

    public NotFoundException(String message) {
        super(message);
    }
}
