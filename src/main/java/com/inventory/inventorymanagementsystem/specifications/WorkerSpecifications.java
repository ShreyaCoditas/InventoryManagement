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




    public static Specification<User> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;

            // Normalize the input to match DB values (ACTIVE / INACTIVE)
            String normalized;
            switch (status.toLowerCase()) {
                case "active", "yes" -> normalized = "ACTIVE";
                case "inactive", "no" -> normalized = "INACTIVE";
                default -> { return null; }
            }

            return cb.equal(cb.upper(root.get("isActive")), normalized);
        };
    }



    public static Specification<User> hasLocation(String location) {
        return (root, query, cb) -> {
            if (location == null || location.isBlank()) return null;

            // Split by comma and normalize (e.g., "Pune,Mumbai" → ["pune", "mumbai"])
            String[] locations = location.split(",");
            List<String> normalized = Arrays.stream(locations)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .toList();

            if (normalized.isEmpty()) return null;

            query.distinct(true);
            Join<User, UserFactoryMapping> mapping = root.join("userFactoryMappings", JoinType.LEFT);
            Join<UserFactoryMapping, Factory> factory = mapping.join("factory", JoinType.LEFT);

            // Combine all locations with OR conditions
            return cb.or(
                    normalized.stream()
                            .map(city -> cb.like(cb.lower(factory.get("city")), "%" + city + "%"))
                            .toArray(Predicate[]::new)

            );
        };
    }

}





