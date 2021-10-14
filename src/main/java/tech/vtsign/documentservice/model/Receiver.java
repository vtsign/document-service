package tech.vtsign.documentservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Receiver {
    private String name;
    private String email;
    private String privateMessage;
    private String permission;
    private String key;
}
