package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.entity.UserCentralOfficeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCentralOfficeRepository extends JpaRepository<UserCentralOfficeMapping, Long> {
    Optional<UserCentralOfficeMapping> findByCentralOfficer(User user);


}
