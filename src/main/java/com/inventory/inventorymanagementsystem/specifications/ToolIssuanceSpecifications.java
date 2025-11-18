package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ToolIssuanceStatus;
import com.inventory.inventorymanagementsystem.entity.ToolIssuance;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ToolIssuanceSpecifications {

    public static Specification<ToolIssuance> belongsToFactory(Long factoryId) {
        return (root, query, cb) -> {
            if (factoryId == null) return cb.conjunction();
            // join toolRequest -> worker -> userFactoryMappings -> factory.id
            var workerJoin = root.join("toolRequest", JoinType.INNER)
                    .join("worker", JoinType.INNER)
                    .join("userFactoryMappings", JoinType.INNER);
            return cb.equal(workerJoin.get("factory").get("id"), factoryId);
        };
    }

    public static Specification<ToolIssuance> hasStatuses(java.util.List<ToolIssuanceStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return cb.conjunction();
            return root.get("issuanceStatus").in(statuses);
        };
    }

    public static Specification<ToolIssuance> searchByToolOrWorker(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String like = "%" + keyword.toLowerCase() + "%";
            var toolJoin = root.join("tool", JoinType.LEFT);
            var workerJoin = root.join("toolRequest", JoinType.LEFT).join("worker", JoinType.LEFT);
            var toolName = cb.lower(toolJoin.get("name"));
            var workerName = cb.lower(workerJoin.get("username"));
            return cb.or(cb.like(toolName, like), cb.like(workerName, like));
        };
    }

    public static Specification<ToolIssuance> isNotPerishable(boolean onlyReturnable) {
        return (root, query, cb) -> {
            if (!onlyReturnable) return cb.conjunction();
            var toolJoin = root.join("tool", JoinType.LEFT);
            return cb.equal(toolJoin.get("isPerishable"), com.inventory.inventorymanagementsystem.constants.IsPerishableEnum.NO);
        };
    }
}
