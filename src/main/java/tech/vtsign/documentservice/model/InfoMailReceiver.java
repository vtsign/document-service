package tech.vtsign.documentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoMailReceiver {
    private String email;
    private String url;
    private String nameSender;
}
