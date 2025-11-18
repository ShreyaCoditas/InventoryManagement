package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.entity.ToolRestockRequest;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class RestockRequestSpecifications {

    public static Specification<ToolRestockRequest> belongsToFactory(Long factoryId) {
        return (root, query, cb) -> {
            if (factoryId == null) return cb.conjunction();

            return cb.equal(
                    root.join("factory", JoinType.INNER).get("id"),
                    factoryId
            );
        };
    }

}
