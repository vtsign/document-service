package tech.vtsign.documentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverContract {
    private Receiver receiver;

    private String mailMessage;
    private String mailTitle;
    private String url;
    private String senderName;
}
