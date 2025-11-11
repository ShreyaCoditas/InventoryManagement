package com.inventory.inventorymanagementsystem.util;

import org.springframework.data.domain.Page;
import java.util.Map;

public class PaginationUtil {

    private PaginationUtil() {
        // prevent instantiation
    }

    public static Map<String, Object> build(Page<?> page) {
        return Map.of(
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages()
        );
    }
}

