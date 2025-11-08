package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.entity.Factory;
import org.springframework.data.jpa.domain.Specification;

public class FactorySpecifications {

    public static Specification<Factory> withFilters(String location, String plantHeadName) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // ✅ Filter by city/location
            if (location != null && !location.isBlank()) {
                predicates.getExpressions().add(
                        cb.like(cb.lower(root.get("city")), "%" + location.toLowerCase() + "%")
                );
            }

            // ✅ Strict Filter by Plant Head name — only factories that have a PlantHead assigned
            if (plantHeadName != null && !plantHeadName.isBlank()) {
                // Use INNER JOIN instead of LEFT JOIN
                var join = root.join("plantHead");
                predicates.getExpressions().add(
                        cb.equal(cb.lower(join.get("username")), plantHeadName.toLowerCase())
                );
            }

            return predicates;
        };
    }
}
