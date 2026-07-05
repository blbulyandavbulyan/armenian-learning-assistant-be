package com.blbulyandavbulyan.larm.user;

import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.User;
import com.blbulyandavbulyan.larm.dao.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Cacheable(value = "users")// TODO is such value even ok here?
    public UUID aquireUserId(String iss, String sub) {
        return userRepository.findByIssAndSub(iss, sub)
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(UUID.randomUUID())
                        .issuer(iss)
                        .subject(sub)
                        .build()))
                .getId();
    }
}
