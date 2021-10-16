package tech.vtsign.documentservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.vtsign.documentservice.model.LoginServerResponseDto;

@FeignClient(name = "user-service")
public interface UserServiceProxy {
    @GetMapping("/user/email")
    LoginServerResponseDto getOrCreateUser(@RequestParam String email, @RequestParam(required = false) String name);

}