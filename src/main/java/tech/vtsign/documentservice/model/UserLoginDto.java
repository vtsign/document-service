package tech.vtsign.documentservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

    @Email(message = "Invalid email address")
    @NotBlank(message = "Missing Email ")
    private String email;
    @NotBlank(message = "Missing Password")
    private String password;
}
