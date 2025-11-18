package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.CentralOfficeProductRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CentralOfficeProductRequestRepository extends JpaRepository<CentralOfficeProductRequest,Long>, JpaSpecificationExecutor<CentralOfficeProductRequest> {
}
