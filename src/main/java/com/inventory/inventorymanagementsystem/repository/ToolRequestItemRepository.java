package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.ToolRequestItemStatus;
import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.entity.ToolRequestItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ToolRequestItemRepository extends JpaRepository<ToolRequestItem,Long>,JpaSpecificationExecutor<ToolRequestItem> {
//    @Query("SELECT i FROM ToolRequestItem i WHERE i.toolRequest.worker.id = :workerId")
//    List<ToolRequestItem> findByWorkerId(Long workerId);
//
//    @Query("SELECT i FROM ToolRequestItem i WHERE i.status = 'PENDING'")
//    List<ToolRequestItem> findPendingItems();
//
//    @Query("SELECT i FROM ToolRequestItem i WHERE i.status = 'SENT_TO_PH'")
//    List<ToolRequestItem> findItemsForPH();
//
//    List<ToolRequestItem> findByStatus(ToolRequestStatus status);

    @Query("SELECT i FROM ToolRequestItem i WHERE i.toolRequest.worker.id = :workerId")
    List<ToolRequestItem> findItemsByWorker(Long workerId);

    List<ToolRequestItem> findByToolRequestId(Long requestId);



}
