package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.Tool;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolSpecifications {

    // ✅ 1. Active only
    public static Specification<Tool> isActive() {
        return (root, query, cb) -> cb.equal(root.get("isActive"), ActiveStatus.ACTIVE);
    }

    // ✅ 2. Category filtering
    public static Specification<Tool> hasCategories(List<String> categoryNames) {
        return (root, query, cb) -> {
            if (categoryNames == null || categoryNames.isEmpty()) return null;

            List<String> normalized = categoryNames.stream()
                    .flatMap(names -> Arrays.stream(names.split(","))) // support comma separated
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            List<Predicate> predicates = new ArrayList<>();
            for (String cat : normalized) {
                predicates.add(cb.like(cb.lower(root.get("category").get("categoryName")), "%" + cat + "%"));
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
