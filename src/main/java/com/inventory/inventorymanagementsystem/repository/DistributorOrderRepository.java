package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.DistributorOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributorOrderRepository extends JpaRepository<DistributorOrder, Long> {
}

