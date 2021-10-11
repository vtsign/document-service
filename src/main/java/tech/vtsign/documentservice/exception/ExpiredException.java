package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 410
public class ExpiredException extends RuntimeException{
    public static final HttpStatus status = HttpStatus.EXPECTATION_FAILED;

    public ExpiredException(String message) {
        super(message);
    }
}
