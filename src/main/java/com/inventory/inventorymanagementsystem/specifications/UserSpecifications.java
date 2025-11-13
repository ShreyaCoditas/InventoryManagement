package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {

    public static Specification<User> withFilters(
            String name,
            ActiveStatus status,
            List<ActiveStatus> statuses,
            LocalDate createdAfter,
            LocalDate createdBefore
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // ✅ Filter by name (case-insensitive)
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + name.toLowerCase() + "%"));
            }

            // ✅ Handle multiple or single status filters
            if (statuses != null && !statuses.isEmpty()) {
                var inClause = cb.in(root.get("isActive"));
                statuses.forEach(inClause::value);
                predicates.add(inClause);
            } else if (status != null) {
                predicates.add(cb.equal(root.get("isActive"), status));
            }

            // ✅ Filter by created date range
            if (createdAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt").as(java.time.LocalDateTime.class),
                        createdAfter.atStartOfDay()
                ));
            }
            if (createdBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt").as(java.time.LocalDateTime.class),
                        createdBefore.atTime(23, 59, 59)
                ));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
