package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.entity.UserFactoryMapping;
import org.springframework.data.jpa.domain.Specification;

public class WorkerSpecifications {

    public static Specification<UserFactoryMapping> withFilters(String location, String status, Long factoryId) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // ✅ Always filter by assigned role = WORKER
            predicates.getExpressions().add(
                    cb.equal(root.get("assignedRole"), RoleName.WORKER)
            );

            // ✅ Optional filter: factoryId
            if (factoryId != null) {
                predicates.getExpressions().add(
                        cb.equal(root.get("factory").get("id"), factoryId)
                );
            }

            // ✅ Optional filter: location (factory city)
            if (location != null && !location.isBlank()) {
                predicates.getExpressions().add(
                        cb.like(cb.lower(root.get("factory").get("city")), "%" + location.toLowerCase() + "%")
                );
            }

            // ✅ Optional filter: worker status
            if (status != null && !status.isBlank()) {
                ActiveStatus activeStatus = ActiveStatus.valueOf(status.toUpperCase());
                predicates.getExpressions().add(
                        cb.equal(root.get("user").get("isActive"), activeStatus)
                );
            }

            return predicates;
        };
    }
}
