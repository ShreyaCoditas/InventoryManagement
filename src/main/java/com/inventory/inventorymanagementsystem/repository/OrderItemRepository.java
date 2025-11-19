package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
