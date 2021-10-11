package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 419
public class MissingFieldException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.valueOf(419);

    public MissingFieldException(String message) {
        super(message);
    }
}
