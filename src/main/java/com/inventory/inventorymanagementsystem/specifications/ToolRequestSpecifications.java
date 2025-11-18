package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.ToolRequestItemStatus;
import com.inventory.inventorymanagementsystem.entity.ToolRequestItem;
import org.springframework.data.jpa.domain.Specification;

public class ToolRequestSpecifications {

    public static Specification<ToolRequestItem> belongsToFactory(Long factoryId) {
        return (root, query, cb) -> cb.equal(
                root.join("toolRequest").join("worker").join("userFactoryMappings").get("factory").get("id"),
                factoryId
        );
    }

    public static Specification<ToolRequestItem> expensiveOnly() {
        return (root, query, cb) -> cb.equal(
                root.get("tool").get("isExpensive"),
                ExpensiveEnum.YES
        );
    }

    public static Specification<ToolRequestItem> hasStatus(String statusStr) {
        return (root, query, cb) -> {
            if (statusStr == null) return cb.conjunction();
            return cb.equal(root.get("status"),
                    ToolRequestItemStatus.valueOf(statusStr.toUpperCase()));
        };
    }
}
