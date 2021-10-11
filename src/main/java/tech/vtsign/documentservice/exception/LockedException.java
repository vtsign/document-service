package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 423
public class LockedException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.LOCKED;

    public LockedException(String message) {
        super(message);
    }
}
