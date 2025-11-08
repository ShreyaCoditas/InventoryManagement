package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withFilters(String name, ActiveStatus status,
                                                  LocalDate createdAfter, LocalDate createdBefore) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ✅ Filter by name (case-insensitive)
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + name.toLowerCase() + "%"));
            }

            // ✅ Filter by active status
            if (status != null) {
                predicates.add(cb.equal(root.get("isActive"), status));
            }

            // ✅ Filter by created date range (only dates)
            if (createdAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter.atStartOfDay()));
            }
            if (createdBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore.atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
