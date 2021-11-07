package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

public class SignedException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.LOCKED;

    public SignedException(String message) {
        super(message);
    }
}
