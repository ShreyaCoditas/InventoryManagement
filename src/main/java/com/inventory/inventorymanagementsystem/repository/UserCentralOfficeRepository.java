package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.UserCentralOfficeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCentralOfficeRepository extends JpaRepository<UserCentralOfficeMapping, Long> {
}
