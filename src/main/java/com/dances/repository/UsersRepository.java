package com.dances.repository;

import com.dances.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {

    // Пошук користувача за іменем (username)
    Optional<Users> findByName(String name);

    // Додатково, якщо треба пошук за email (опціонально)
    Optional<Users> findByEmail(String email);

    boolean existsByName(String name);

    boolean existsByEmail(String email);
}
