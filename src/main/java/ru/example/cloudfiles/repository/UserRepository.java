package ru.example.cloudfiles.repository;


import org.springframework.data.repository.CrudRepository;
import ru.example.cloudfiles.entity.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findUserByUsername(String name);
}
