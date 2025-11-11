package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.Factory;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class FactorySpecifications {
    public static Specification<Factory> withFilters(String location, String plantHeadName, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            //  Filter by city/location
            if (location != null && !location.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("city")), "%" + location.trim().toLowerCase() + "%")
                );
            }

            // Filter by Plant Head name (exact match)
            if (plantHeadName != null && !plantHeadName.isBlank()) {
                var join = root.join("plantHead", JoinType.INNER);
                predicates.add(
                        cb.equal(cb.lower(join.get("username")), plantHeadName.trim().toLowerCase())
                );
            }

            // ✅ Filter by Active/Inactive status
            if (status != null && !status.isBlank()) {
                try {
                    ActiveStatus activeStatus = ActiveStatus.valueOf(status.trim().toUpperCase());
                    predicates.add(cb.equal(root.get("isActive"), activeStatus));
                } catch (IllegalArgumentException ignored) {
                    // ignore invalid status
                }
            }

            // ✅ Combine all filters with AND
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }






}
