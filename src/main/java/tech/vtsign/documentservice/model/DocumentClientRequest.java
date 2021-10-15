package tech.vtsign.documentservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentClientRequest {
    @JsonProperty("mail_title")
    private String mailTitle;
    @JsonProperty("mail_message")
    private String mailMessage;
    private List<Receiver> receivers;
}
