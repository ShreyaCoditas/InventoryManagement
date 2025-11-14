package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.entity.ToolRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolRequestRepository extends JpaRepository<ToolRequest,Long> {


    List<ToolRequest> findByStatus(ToolRequestStatus status);

    List<ToolRequest> findByWorkerId(Long workerId);

}
