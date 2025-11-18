package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.entity.Tool;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToolSpecifications {

    public static Specification<Tool> isActive() {
        return (root, query, cb) ->
                cb.equal(root.get("isActive"), com.inventory.inventorymanagementsystem.constants.ActiveStatus.ACTIVE);
    }

    public static Specification<Tool> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    public static Specification<Tool> hasCategories(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) return null;

            List<String> normalized = new ArrayList<>();
            categories.forEach(cat ->
                    Arrays.stream(cat.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toLowerCase)
                            .forEach(normalized::add)
            );

            List<Predicate> predicates = new ArrayList<>();
            for (String cat : normalized) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("category").get("categoryName")),
                                "%" + cat + "%"
                        )
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    // ⭐ MULTIPLE LOCATION FILTER
    public static Specification<Tool> hasMultipleLocations(List<String> locations) {
        return (root, query, cb) -> {
            if (locations == null || locations.isEmpty()) return null;

            // Tool -> ToolStock -> Factory
            Join<Object, Object> stockJoin = root.join("toolStocks", JoinType.LEFT);
            Join<Object, Object> factoryJoin = stockJoin.join("factory", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            for (String loc : locations) {
                predicates.add(
                        cb.like(
                                cb.lower(factoryJoin.get("address")),
                                "%" + loc.toLowerCase() + "%"
                        )
                );
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Tool> hasFactoryNames(List<String> factoryNames) {
        return (root, query, cb) -> {

            if (factoryNames == null || factoryNames.isEmpty()) return null;

            List<String> normalized = factoryNames.stream()
                    .flatMap(n -> Arrays.stream(n.split(",")))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            // JOIN Tool → ToolStock
            Join<Object, Object> ts = root.join("toolStocks", JoinType.LEFT);
            // JOIN ToolStock → Factory
            Join<Object, Object> fac = ts.join("factory", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            for (String f : normalized) {
                predicates.add(cb.like(cb.lower(fac.get("name")), "%" + f + "%"));
            }

            query.distinct(true);   // avoid duplicates when multiple stocks exist

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }


}
