package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.Product;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    void deleteByCategory(ProductCategory category);

    @Query("SELECT COALESCE(SUM(fis.quantity),0) " +
            "FROM FactoryInventoryStock fis " +
            "WHERE fis.product.id = :productId")
    Integer findTotalQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(fis.quantity),0) " +
            "FROM FactoryInventoryStock fis " +
            "WHERE fis.product.id = :productId AND fis.factory.id = :factoryId")
    Integer findTotalQuantityByProductIdAndFactoryId(
            @Param("productId") Long productId,
            @Param("factoryId") Long factoryId);


    // Find all active products
    List<Product> findByIsActive(ActiveStatus status);

    // Optional: Find all products by category name (useful for filtering)
    List<Product> findByCategory_CategoryNameIgnoreCaseAndIsActive(String categoryName, ActiveStatus status);

    // Optional: Search products by name (for frontend search bar)
    List<Product> findByNameContainingIgnoreCaseAndIsActive(String name, ActiveStatus status);
}

