package tech.vtsign.documentservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExceptionResponse {
    private Date timestamp;
    private String message;
    private String details;
    private int status;


    public ExceptionResponse(int status, Map<String, Object> errorAttributes) {
        this.setStatus(status);
        this.setMessage((String) errorAttributes.get("error"));
        this.setTimestamp((Date) errorAttributes.get("timestamp"));
        this.setDetails((String) errorAttributes.get("path"));
    }

}
