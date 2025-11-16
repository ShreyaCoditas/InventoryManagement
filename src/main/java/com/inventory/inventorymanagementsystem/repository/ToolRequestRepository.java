package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.entity.ToolRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolRequestRepository extends JpaRepository<ToolRequest,Long> {


   // List<ToolRequest> findByStatus(ToolRequestStatus status);

    //List<ToolRequest> findByWorkerId(Long workerId);

    // ToolRequestRepository.java
    //Page<ToolRequest> findByFactoryId(Long factoryId, Pageable pageable);
    //Page<ToolRequest> findByFactoryIdAndStatus(Long factoryId, ToolRequestStatus status, Pageable pageable);



}
