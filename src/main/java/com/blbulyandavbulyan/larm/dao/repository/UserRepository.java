package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findByIssAndSub(String iss, String sub);
}
