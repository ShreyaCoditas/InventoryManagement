package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.OrderBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderBatchRepository extends JpaRepository<OrderBatch,Long> {
    List<OrderBatch> findByOrderItemId(Integer orderItemId);

    // Fetch batches by order id
    List<OrderBatch> findByOrder_Id(Long orderId);

    // Fetch batches by orderItem id
    List<OrderBatch> findByOrderItem_Id(Long orderItemId);

}
