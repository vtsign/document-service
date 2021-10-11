package tech.vtsign.documentservice.exception;

import org.springframework.http.HttpStatus;

// 403
public class UnauthorizedException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.FORBIDDEN;

    public UnauthorizedException(String message) {
        super(message);
    }
}
