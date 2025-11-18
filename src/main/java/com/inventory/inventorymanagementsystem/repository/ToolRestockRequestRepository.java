
package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.ToolRestockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ToolRestockRequestRepository
        extends JpaRepository<ToolRestockRequest, Long>,
        JpaSpecificationExecutor<ToolRestockRequest> {

}



