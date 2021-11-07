package tech.vtsign.documentservice.service;

import tech.vtsign.documentservice.domain.User;

import java.util.UUID;

public interface UserService {
    User findById(UUID userUUID);
}
