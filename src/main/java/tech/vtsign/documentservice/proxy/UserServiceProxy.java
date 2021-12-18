package tech.vtsign.documentservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.vtsign.documentservice.model.Item;
import tech.vtsign.documentservice.model.LoginServerResponseDto;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceProxy {
    @GetMapping("/user/apt/email")
    LoginServerResponseDto getOrCreateUser(@RequestParam String email, @RequestParam(required = false) String phone, @RequestParam(required = false) String name);

    @GetMapping("/user/apt/uuid")
    LoginServerResponseDto getUserById(@RequestParam("user_uuid") UUID uuid);

    @PostMapping("/user/apt/payment")
    Boolean paymentForSendDocument(@RequestBody Item item);

}
