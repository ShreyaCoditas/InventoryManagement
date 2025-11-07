package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory,Long> {

    Optional<ProductCategory> findByCategoryNameIgnoreCase(String categoryName);
}

