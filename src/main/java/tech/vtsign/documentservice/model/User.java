package tech.vtsign.documentservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.UUID;


@Builder
@Getter
@Setter
public class User {
    @Id
    @Column(name = "user_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String email;
}
