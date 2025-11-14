package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.entity.Merchandise;
import org.springframework.data.jpa.domain.Specification;

public class MerchandiseSpecifications {

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
}

