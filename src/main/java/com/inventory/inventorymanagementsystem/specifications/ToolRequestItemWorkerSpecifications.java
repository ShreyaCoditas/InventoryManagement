package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ToolRequestItemStatus;
import com.inventory.inventorymanagementsystem.entity.ToolRequestItem;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ToolRequestItemWorkerSpecifications {

    public static Specification<ToolRequestItem> belongsToWorker(Long workerId) {
        return (root, query, cb) -> {
            if (workerId == null) return cb.conjunction();
            return cb.equal(
                    root.join("toolRequest", JoinType.INNER)
                            .join("worker", JoinType.INNER)
                            .get("id"),
                    workerId
            );
        };
    }

    public static Specification<ToolRequestItem> hasStatus(String statusStr) {
        return (root, query, cb) -> {
            if (statusStr == null || statusStr.isBlank()) return cb.conjunction();

            try {
                ToolRequestItemStatus status = ToolRequestItemStatus.valueOf(statusStr.toUpperCase());
                return cb.equal(root.get("status"), status);
            } catch (IllegalArgumentException e) {
                return cb.disjunction(); // invalid → return zero results
            }
        };
    }

    public static Specification<ToolRequestItem> searchByToolName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return cb.conjunction();
            return cb.like(
                    cb.lower(root.join("tool", JoinType.LEFT).get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }
}

