package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 419
public class ConflictException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.CONFLICT;

    public ConflictException(String message) {
        super(message);
    }

}
