package tech.vtsign.documentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoMailReceiver {
    private String name;
    private String email;
    private String privateMessage;

    private String mailMessage;
    private String mailTitle;
    private String url;
    private String senderName;
}
