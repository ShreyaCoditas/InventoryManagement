package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.entity.CentralOfficeInventory;
import org.springframework.data.jpa.domain.Specification;

public class CentralOfficeInventorySpecifications {

    // Filter by product ID
    public static Specification<CentralOfficeInventory> hasProductId(Long productId) {
        return (root, query, cb) ->
                productId == null ? null :
                        cb.equal(root.get("product").get("id"), productId);
    }

    // Search by product name (case-insensitive)
    public static Specification<CentralOfficeInventory> searchByProductName(String search) {
        return (root, query, cb) ->
                (search == null || search.isBlank()) ? null :
                        cb.like(cb.lower(root.get("product").get("name")),
                                "%" + search.toLowerCase() + "%");
    }

    // Minimum quantity filter
    public static Specification<CentralOfficeInventory> minQuantity(Long minQuantity) {
        return (root, query, cb) ->
                minQuantity == null ? null :
                        cb.greaterThanOrEqualTo(root.get("quantity"), minQuantity);
    }

    // Maximum quantity filter
    public static Specification<CentralOfficeInventory> maxQuantity(Long maxQuantity) {
        return (root, query, cb) ->
                maxQuantity == null ? null :
                        cb.lessThanOrEqualTo(root.get("quantity"), maxQuantity);
    }

    // Filter only active products (optional)
    public static Specification<CentralOfficeInventory> productIsActive(Boolean activeOnly) {
        return (root, query, cb) ->
                (activeOnly == null || !activeOnly) ? null :
                        cb.equal(root.get("product").get("isActive"), "ACTIVE");
    }
}
