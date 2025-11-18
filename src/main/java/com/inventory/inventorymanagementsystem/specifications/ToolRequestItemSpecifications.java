package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.ToolRequestItemStatus;
import com.inventory.inventorymanagementsystem.entity.ToolRequestItem;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ToolRequestItemSpecifications {
    public static Specification<ToolRequestItem> belongsToFactory(Long factoryId) {
        return (root, query, cb) -> {
            if (factoryId == null) return cb.conjunction();
            return cb.equal(
                    root.join("toolRequest")
                            .join("worker")
                            .join("userFactoryMappings")   // worker → mapping
                            .get("factory")
                            .get("id"),
                    factoryId
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
                    return cb.disjunction();
                }
            };
        }

        public static Specification<ToolRequestItem> hasExpensiveTag(String expensiveStr) {
            return (root, query, cb) -> {
                if (expensiveStr == null || expensiveStr.isBlank()) return cb.conjunction();
                try {
                    ExpensiveEnum expensive = ExpensiveEnum.valueOf(expensiveStr.toUpperCase());
                    return cb.equal(root.get("tool").get("isExpensive"), expensive);
                } catch (IllegalArgumentException e) {
                    return cb.disjunction();
                }
            };
        }
    }

