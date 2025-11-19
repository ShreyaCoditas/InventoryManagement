package com.inventory.inventorymanagementsystem.specifications;


import com.inventory.inventorymanagementsystem.entity.CentralOfficeInventory;
import org.springframework.data.jpa.domain.Specification;

public class CentralInventorySpecifications {

    // Search by product name
    public static Specification<CentralOfficeInventory> nameContains(String search) {
        return (root, query, cb) ->
                (search == null || search.isBlank())
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("product").get("name")),
                        "%" + search.toLowerCase() + "%");
    }

    // Filter by category
    public static Specification<CentralOfficeInventory> categoryEquals(Long categoryId) {
        return (root, query, cb) ->
                (categoryId == null)
                        ? cb.conjunction()
                        : cb.equal(root.get("product").get("category").get("id"), categoryId);
    }

    // Min price
    public static Specification<CentralOfficeInventory> minPrice(Double min) {
        return (root, query, cb) ->
                (min == null)
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get("product").get("price"), min);
    }

    // Max price
    public static Specification<CentralOfficeInventory> maxPrice(Double max) {
        return (root, query, cb) ->
                (max == null)
                        ? cb.conjunction()
                        : cb.lessThanOrEqualTo(root.get("product").get("price"), max);
    }
}

