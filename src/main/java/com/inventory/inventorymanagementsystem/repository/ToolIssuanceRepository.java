package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.ToolIssuance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ToolIssuanceRepository extends JpaRepository<ToolIssuance,Long>, JpaSpecificationExecutor<ToolIssuance> {
}
