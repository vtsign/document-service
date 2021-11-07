package tech.vtsign.documentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.documentservice.domain.User;
import tech.vtsign.documentservice.exception.NotFoundException;
import tech.vtsign.documentservice.repository.UserRepository;
import tech.vtsign.documentservice.service.UserService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User findById(UUID userUUID) {
        Optional<User> user = userRepository.findById(userUUID);
        return user.orElseThrow(() -> new NotFoundException("user Not Found"));
    }
}
