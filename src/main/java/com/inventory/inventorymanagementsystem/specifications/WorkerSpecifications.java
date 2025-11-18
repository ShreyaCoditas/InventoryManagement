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

//    public static Specification<User> hasStatus(String status) {
//        return (root, query, cb) -> {
//            if (status == null || status.isBlank()) return null;
//
//            // Normalize the input to match DB values (ACTIVE / INACTIVE)
//            String normalized;
//            switch (status.toLowerCase()) {
//                case "active", "yes" -> normalized = "ACTIVE";
//                case "inactive", "no" -> normalized = "INACTIVE";
//                default -> { return null; }
//            }
//
//            return cb.equal(cb.upper(root.get("isActive")), normalized);
//        };
//    }

    public static Specification<User> belongsToFactory(Long factoryId) {
        return (root, query, cb) -> {
            if (factoryId == null) return null;

            // JOIN user_factory_mapping table
            Join<User, UserFactoryMapping> mapping = root.join("userFactoryMappings", JoinType.INNER);

            return cb.equal(mapping.get("factory").get("id"), factoryId);
        };
    }



//
//    public static Specification<User> hasLocation(String location) {
//        return (root, query, cb) -> {
//            if (location == null || location.isBlank()) return null;
//
//            // Split by comma and normalize (e.g., "Pune,Mumbai" → ["pune", "mumbai"])
//            String[] locations = location.split(",");
//            List<String> normalized = Arrays.stream(locations)
//                    .map(String::trim)
//                    .filter(s -> !s.isEmpty())
//                    .map(String::toLowerCase)
//                    .toList();
//
//            if (normalized.isEmpty()) return null;
//
//            query.distinct(true);
//            Join<User, UserFactoryMapping> mapping = root.join("userFactoryMappings", JoinType.LEFT);
//            Join<UserFactoryMapping, Factory> factory = mapping.join("factory", JoinType.LEFT);
//
//            // Combine all locations with OR conditions
//            return cb.or(
//                    normalized.stream()
//                            .map(city -> cb.like(cb.lower(factory.get("city")), "%" + city + "%"))
//                            .toArray(Predicate[]::new)
//
//            );
//        };
//    }
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





