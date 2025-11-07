package com.inventory.inventorymanagementsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.inventorymanagementsystem.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

}
