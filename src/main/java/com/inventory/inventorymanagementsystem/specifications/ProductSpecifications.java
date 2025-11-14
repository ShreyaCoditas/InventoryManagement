package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.FactoryInventoryStock;
import com.inventory.inventorymanagementsystem.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProductSpecifications {

    public static Specification<Product> withFilters(
            List<String> categoryNames,
            String availability,
            String status,
            Long factoryId,
            Long productId
    ) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // Status
            if (status != null && !status.isBlank()) {
                try {
                    predicates.getExpressions().add(
                            cb.equal(root.get("isActive"), ActiveStatus.valueOf(status.toUpperCase()))
                    );
                } catch (IllegalArgumentException ignored) {}
            } else {
                predicates.getExpressions().add(cb.equal(root.get("isActive"), ActiveStatus.ACTIVE));
            }

            // Category filter
            if (categoryNames != null && !categoryNames.isEmpty()) {
                var join = root.join("category", JoinType.LEFT);
                var lowerNames = categoryNames.stream().map(String::toLowerCase).toList();
                predicates.getExpressions().add(
                        cb.lower(join.get("categoryName")).in(lowerNames)
                );
            }

            // ✅ Factory filter (join inventory stock)
            if (factoryId != null) {
                Join<Product, FactoryInventoryStock> stockJoin =
                        root.join("factoryInventoryStocks", JoinType.LEFT);
                predicates.getExpressions().add(
                        cb.equal(stockJoin.get("factory").get("id"), factoryId)
                );
                query.distinct(true);
            }

            // ✅ Product ID filter
            if (productId != null) {
                predicates.getExpressions().add(cb.equal(root.get("id"), productId));
            }

            return predicates;
        };
    }
}
