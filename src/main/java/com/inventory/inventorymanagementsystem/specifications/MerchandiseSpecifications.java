package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.Merchandise;
import org.springframework.data.jpa.domain.Specification;

public class MerchandiseSpecifications {

    public static Specification<Merchandise> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return cb.conjunction();
            return cb.equal(root.get("isActive"), ActiveStatus.valueOf(status.toUpperCase()));
        };
    }

    public static Specification<Merchandise> searchByName(String search) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<Merchandise> hasMinRewardPoints(Integer points) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("rewardPoints"), points);
    }

    public static Specification<Merchandise> hasMaxRewardPoints(Integer points) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("rewardPoints"), points);
    }

    public static Specification<Merchandise> hasStockStatus(String stockStatus) {

        if (stockStatus == null || stockStatus.isBlank())
            return (root, query, cb) -> cb.conjunction();

        return (root, query, cb) -> {
            if (stockStatus.equalsIgnoreCase("INSTOCK")) {
                return cb.greaterThan(root.get("quantity"), 0);
            } else if (stockStatus.equalsIgnoreCase("OUTOFSTOCK")) {
                return cb.equal(root.get("quantity"), 0);
            }
            return cb.conjunction();
        };
    }
}
