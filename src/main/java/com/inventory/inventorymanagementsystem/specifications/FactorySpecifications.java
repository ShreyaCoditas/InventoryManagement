package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.Factory;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class FactorySpecifications {

    public static Specification<Factory> withFilters(
            List<String> locations,
            String plantHeadName,
            String status
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            /* 🔥 MULTIPLE LOCATION FILTER (Supports Pune,Mumbai,Hyd) */
            if (locations != null && !locations.isEmpty()) {

                List<String> normalized = locations.stream()
                        .flatMap(loc -> List.of(loc.split(",")).stream())  // Split Pune,Mumbai
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();

                predicates.add(cb.lower(root.get("city")).in(normalized));
            }

            /* 🔥 FILTER BY PLANT HEAD NAME */
            if (plantHeadName != null && !plantHeadName.isBlank()) {
                var join = root.join("plantHead", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                cb.lower(join.get("username")),
                                plantHeadName.trim().toLowerCase()
                        )
                );
            }

            /* 🔥 FILTER BY STATUS (ACTIVE / INACTIVE) */
            if (status != null && !status.isBlank()) {
                try {
                    ActiveStatus st = ActiveStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("isActive"), st));
                } catch (IllegalArgumentException ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
