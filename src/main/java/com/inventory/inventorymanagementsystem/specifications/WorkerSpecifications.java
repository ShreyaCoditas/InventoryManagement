package com.inventory.inventorymanagementsystem.specifications;
import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.Role;
import com.inventory.inventorymanagementsystem.entity.User;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.entity.UserFactoryMapping;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;


import java.util.Arrays;
import java.util.List;

public class WorkerSpecifications {


    public static Specification<User> isWorker() {
        return (root, query, cb) -> {
            Join<User, Role> roleJoin = root.join("role", JoinType.LEFT);
            // Cast enum to text before comparing
            return cb.equal(
                    cb.function("text", String.class, roleJoin.get("roleName")),
                    RoleName.WORKER.name()
            );
        };
    }
    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
    }



    public static Specification<User> hasStatuses(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.equal(cb.upper(root.get("isActive")), "ACTIVE"); // default to active
            }

            List<String> normalized = statuses.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .map(s -> switch (s) {
                        case "active", "yes" -> "ACTIVE";
                        case "inactive", "no" -> "INACTIVE";
                        default -> null;
                    })
                    .filter(s -> s != null)
                    .toList();

            return cb.or(
                    normalized.stream()
                            .map(state -> cb.equal(cb.upper(root.get("isActive")), state))
                            .toArray(Predicate[]::new)
            );
        };

    }


    public static Specification<User> belongsToFactory(Long factoryId) {
        return (root, query, cb) -> {
            if (factoryId == null) return null;

            // JOIN user_factory_mapping table
            Join<User, UserFactoryMapping> mapping = root.join("userFactoryMappings", JoinType.INNER);

            return cb.equal(mapping.get("factory").get("id"), factoryId);
        };
    }

public static Specification<User> hasLocations(List<String> locations) {
    return (root, query, cb) -> {
        if (locations == null || locations.isEmpty()) return null;

        List<String> normalized = locations.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

        query.distinct(true);
        Join<User, UserFactoryMapping> mapping = root.join("userFactoryMappings", JoinType.LEFT);
        Join<UserFactoryMapping, Factory> factory = mapping.join("factory", JoinType.LEFT);

        return cb.or(
                normalized.stream()
                        .map(city -> cb.like(cb.lower(factory.get("city")), "%" + city + "%"))
                        .toArray(Predicate[]::new)
        );
    };
}


}





