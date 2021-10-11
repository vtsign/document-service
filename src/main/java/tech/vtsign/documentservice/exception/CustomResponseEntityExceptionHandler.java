package tech.vtsign.documentservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@RestControllerAdvice
@Slf4j
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public final ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error(ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<ExceptionResponse> badRequestException(BadRequestException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), BadRequestException.status.value());

        return new ResponseEntity<>(exceptionResponse, BadRequestException.status);
    }
    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<ExceptionResponse> handleConflictException(ConflictException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), ConflictException.status.value());

        return new ResponseEntity<>(exceptionResponse, ConflictException.status);
    }
    //3
    @ExceptionHandler(ExpiredException.class)
    public final ResponseEntity<ExceptionResponse> handleExpiredException(ExpiredException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), ExpiredException.status.value());

        return new ResponseEntity<>(exceptionResponse, ExpiredException.status);
    }
    @ExceptionHandler(InvalidFormatException.class)
    public final ResponseEntity<ExceptionResponse> invalidFormatException(InvalidFormatException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), InvalidFormatException.status.value());

        return new ResponseEntity<>(exceptionResponse, InvalidFormatException.status);
    }
    //5
    @ExceptionHandler(LockedException.class)
    public final ResponseEntity<ExceptionResponse> lockedException(LockedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), LockedException.status.value());

        return new ResponseEntity<>(exceptionResponse, LockedException.status);
    }
    @ExceptionHandler(MissingFieldException.class)
    public final ResponseEntity<ExceptionResponse> handleMissingFieldException(MissingFieldException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), MissingFieldException.status.value());

        return new ResponseEntity<>(exceptionResponse, MissingFieldException.status);
    }
    @ExceptionHandler(UnauthorizedException.class)
    public final ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), UnauthorizedException.status.value());

        return new ResponseEntity<>(exceptionResponse, UnauthorizedException.status);
    }
    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ExceptionResponse> handleNotFoundException(UnauthorizedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(), ex.getMessage(), request.getDescription(false), NotFoundException.status.value());

        return new ResponseEntity<>(exceptionResponse, NotFoundException.status);
    }
}
