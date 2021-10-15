package tech.vtsign.documentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestException extends RuntimeException {
    private Date timestamp;
    private String message;
    private String details;
    private int status;
}