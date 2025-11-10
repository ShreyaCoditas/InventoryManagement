package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.Merchandise;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchandiseRepository extends JpaRepository<Merchandise, Long>, JpaSpecificationExecutor<Merchandise> {
    List<Merchandise> findByIsActive(ActiveStatus status);
    boolean existsByNameIgnoreCase(String name);
}
