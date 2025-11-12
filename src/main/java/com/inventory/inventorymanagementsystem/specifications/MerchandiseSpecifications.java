package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.entity.Merchandise;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
public class MerchandiseSpecifications {

    public static Specification<Merchandise> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Merchandise> hasStatus(ActiveStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("isActive"), status);
    }

    public static Specification<Merchandise> minRewardPoints(Integer value) {
        return (root, query, cb) ->
                value == null ? null : cb.greaterThanOrEqualTo(root.get("rewardPoints"), value);
    }

    public static Specification<Merchandise> maxRewardPoints(Integer value) {
        return (root, query, cb) ->
                value == null ? null : cb.lessThanOrEqualTo(root.get("rewardPoints"), value);
    }

    public static Specification<Merchandise> minQuantity(Integer value) {
        return (root, query, cb) ->
                value == null ? null : cb.greaterThanOrEqualTo(root.get("quantity"), value);
    }

    public static Specification<Merchandise> maxQuantity(Integer value) {
        return (root, query, cb) ->
                value == null ? null : cb.lessThanOrEqualTo(root.get("quantity"), value);
    }

    public static Specification<Merchandise> createdAfter(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<Merchandise> createdBefore(LocalDateTime date) {
        return (root, query, cb) ->
                date == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), date);
    }
}
