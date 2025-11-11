package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecifications {

    public static Specification<Product> withFilters(
            String categoryName,
            String availability,
            String status,
            Long productId
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // ✅ Filter by isActive
            if (status != null && !status.isBlank()) {
                try {
                    predicates.getExpressions().add(
                            cb.equal(root.get("isActive"), ActiveStatus.valueOf(status.toUpperCase()))
                    );
                } catch (IllegalArgumentException ignored) {
                }
            } else {
                predicates.getExpressions().add(cb.equal(root.get("isActive"), ActiveStatus.ACTIVE));
            }

            // ✅ Filter by category name
            if (categoryName != null && !categoryName.isBlank()) {
                var join = root.join("category", JoinType.LEFT);
                predicates.getExpressions().add(
                        cb.like(cb.lower(join.get("categoryName")), "%" + categoryName.toLowerCase() + "%")
                );
            }

            // ✅ Filter by productId
            if (productId != null) {
                predicates.getExpressions().add(cb.equal(root.get("id"), productId));
            }

            return predicates;
        };
    }
}
