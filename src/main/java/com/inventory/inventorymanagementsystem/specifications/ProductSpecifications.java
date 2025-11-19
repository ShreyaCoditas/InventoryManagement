package com.inventory.inventorymanagementsystem.specifications;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.FactoryInventoryStock;
import com.inventory.inventorymanagementsystem.entity.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ProductSpecifications {


    public static Specification<Product> isActive() {
        return (root, query, cb) ->
                cb.equal(root.get("isActive"), ActiveStatus.ACTIVE);
    }

    public static Specification<Product> hasStatuses(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                // default → ACTIVE only
                return cb.equal(root.get("isActive"), ActiveStatus.ACTIVE);
            }

            // normalize list
            List<ActiveStatus> activeStatuses = statuses.stream()
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(s -> {
                        try { return ActiveStatus.valueOf(s); }
                        catch (Exception e) { return null; }
                    })
                    .filter(s -> s != null)
                    .toList();

            if (activeStatuses.isEmpty()) {
                return cb.equal(root.get("isActive"), ActiveStatus.ACTIVE);
            }

            return root.get("isActive").in(activeStatuses);
        };
    }



    public static Specification<Product> inCategories(List<String> categoryNames) {
        return (root, query, cb) -> {
            if (categoryNames == null || categoryNames.isEmpty()) return null;

            var join = root.join("category", JoinType.LEFT);

            List<String> normalized = categoryNames.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();

            return cb.or(
                    normalized.stream()
                            .map(cat -> cb.equal(cb.lower(join.get("categoryName")), cat))
                            .toArray(Predicate[]::new)
            );
        };
    }

    public static Specification<Product> availability(String filter) {
        return (root, query, cb) -> {

            if (filter == null || filter.isBlank()) return null;

            Join<Product, FactoryInventoryStock> stock =
                    root.join("factoryInventoryStocks", JoinType.LEFT);

            // Group by product
            query.groupBy(root.get("id"));

            var sumQty = cb.coalesce(cb.sum(stock.get("quantity")), 0);

            Predicate havingPredicate = null;

            switch (filter.toLowerCase()) {
                case "instock", "in-stock" ->
                        havingPredicate = cb.greaterThan(sumQty, 0);

                case "outofstock", "out-of-stock" ->
                        havingPredicate = cb.equal(sumQty, 0);

                default -> {
                    return null;
                }
            }


            query.having(havingPredicate);


            return cb.conjunction();
        };
    }




    /** SEARCH FILTER */
    public static Specification<Product> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String like = "%" + keyword.toLowerCase() + "%";

            Join<Object, Object> category = root.join("category", JoinType.LEFT);

            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("productDescription")), like),
                    cb.like(cb.lower(category.get("categoryName")), like)
            );
        };
    }

    /** SORT BY QUANTITY (computed) */
    public static Specification<Product> sortByQuantity(String sortDir) {
        return (root, query, cb) -> {
            Join<Product, FactoryInventoryStock> stock =
                    root.join("factoryInventoryStocks", JoinType.LEFT);

            query.groupBy(root.get("id"));

            var sumQty = cb.coalesce(cb.sum(stock.get("quantity")), 0);

            if ("desc".equalsIgnoreCase(sortDir)) {
                query.orderBy(cb.desc(sumQty));
            } else {
                query.orderBy(cb.asc(sumQty));
            }

            return cb.conjunction();
        };
    }

}
