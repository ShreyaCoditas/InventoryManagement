package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.Product;
import com.inventory.inventorymanagementsystem.entity.ProductCategory;
import com.inventory.inventorymanagementsystem.repository.FactoryInventoryRepository;
import com.inventory.inventorymanagementsystem.repository.ProductCategoryRepository;
import com.inventory.inventorymanagementsystem.repository.ProductRepository;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class ProductService {

    @Autowired
    private  ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private FactoryInventoryRepository factoryInventoryRepository;


    @Transactional
    public ApiResponseDto<ProductResponseDto> createOrUpdateProduct(CreateOrUpdateProductDto request) {
        ProductCategory category;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Invalid category ID: " + request.getCategoryId()));
        }
        else if (request.getNewCategoryName() != null && !request.getNewCategoryName().isBlank()) {
            String normalizedCategoryName = normalizeCategoryName(request.getNewCategoryName());
            category = categoryRepository.findByCategoryNameIgnoreCase(normalizedCategoryName)
                    .orElseGet(() -> {
                        ProductCategory newCat = new ProductCategory();
                        newCat.setCategoryName(normalizedCategoryName);
                        newCat.setCategoryDescription("Auto-created from product form");
                        newCat.setCreatedAt(LocalDateTime.now());
                        newCat.setUpdatedAt(LocalDateTime.now());
                        return categoryRepository.save(newCat);
                    });
        } else {
            throw new RuntimeException("Either categoryId or newCategoryName must be provided");
        }

        if (request.getImage() != null && !request.getImage().startsWith("http")) {
            throw new RuntimeException("Invalid image URL. Please upload via /upload-image first.");
        }
        Product product;
        if (request.getId() != null) {

            product = productRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getId()));

            if (request.getName() != null) product.setName(request.getName());
            if (request.getProductDescription() != null) product.setProductDescription(request.getProductDescription());
            if (request.getPrice() != null) product.setPrice(request.getPrice());
            if (request.getImage() != null) product.setImage(request.getImage());
            if (category != null) product.setCategory(category);

            product.setRewardPoint(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(0.001))
                            .setScale(0, BigDecimal.ROUND_CEILING)
                            .intValue()
            );

            product.setUpdatedAt(LocalDateTime.now());

        } else {
            product = new Product();
            product.setName(request.getName());
            product.setProductDescription(request.getProductDescription());
            product.setCategory(category);
            product.setPrice(request.getPrice());

            // Calculate reward points
            product.setRewardPoint(
                    request.getPrice()
                            .multiply(BigDecimal.valueOf(0.001))
                            .setScale(0, BigDecimal.ROUND_CEILING)
                            .intValue()
            );

            product.setImage(request.getImage()); // ✅ Cloudinary URL
            product.setIsActive(ActiveStatus.ACTIVE);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
        }

        // ✅ Step 4: Save Product
        Product savedProduct = productRepository.save(product);

        // ✅ Step 5: Build Response DTO
        ProductResponseDto response = new ProductResponseDto(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getProductDescription(),
                savedProduct.getPrice(),
                savedProduct.getRewardPoint(),
                savedProduct.getCategory().getCategoryName(),
                savedProduct.getImage(), // ✅ Cloudinary URL
                savedProduct.getIsActive().name()
        );

        String message = (request.getId() == null)
                ? "Product created successfully"
                : "Product updated successfully";

        return new ApiResponseDto<>(true, message, response);
    }


    /**
     * GET ALL Products with filter + sort
     */
//    public ApiResponseDto<List<ProductResponseDto>> getAllProducts(ProductFilterSortDto filter) {
//
//        List<Product> products = productRepository.findAll().stream()
//                .filter(p -> p.getIsActive() == ActiveStatus.YES)
//                .toList();
//
//        //  Filter by category if needed
//        if (filter.getCategoryName() != null && !filter.getCategoryName().isBlank()) {
//            products = products.stream()
//                    .filter(p -> p.getCategory().getCategoryName()
//                            .equalsIgnoreCase(filter.getCategoryName()))
//                    .toList();
//        }
//
//        // Sort logic (optional)
//        if (filter.getSortBy() != null) {
//            switch (filter.getSortBy()) {
//                case "priceLowHigh" ->
//                        products = products.stream().sorted(Comparator.comparing(Product::getPrice)).toList();
//                case "priceHighLow" ->
//                        products = products.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).toList();
//            }
//        }
//
//        //  Now join inventory stock quantities
//        List<ProductResponseDto> result = products.stream().map(p -> {
//            // Sum quantity from factories_inventory_stock table
//            Integer totalQty = factoryInventoryRepository.findTotalQuantityByProductId(p.getId());
//
//            // Handle null (if no stock entry exists yet)
//            int totalQuantity = (totalQty == null) ? 0 : totalQty;
//
//            // Decide stock status
//            String stockStatus = totalQuantity > 0 ? "In Stock" : "Out of Stock";
//
//            return new ProductResponseDto(
//                    p.getId(),
//                    p.getName(),
//                    p.getProductDescription(),
//                    p.getPrice(),
//                    p.getRewardPoint(),
//                    p.getCategory().getCategoryName(),
//                    p.getImage(),
//                    p.getIsActive().name(),
//                    totalQuantity,      // new field
//                    stockStatus         // new field
//            );
//        }).toList();
//
//        return new ApiResponseDto<>(true, "Products fetched successfully", result);
//    }

    public ApiResponseDto<List<ProductInventoryResponseDto>> getAllProducts(
            ProductFilterSortDto filter, Long factoryId) {   // ✅ Added factoryId param

        // ✅ Step 1: Pagination & Sorting
        Sort sort;
        if ("quantity".equalsIgnoreCase(filter.getSortBy())) {
            // Manual sort later for quantity
            sort = Sort.by("id");
        } else if ("price".equalsIgnoreCase(filter.getSortBy())) {
            sort = "desc".equalsIgnoreCase(filter.getSortDirection())
                    ? Sort.by("price").descending()
                    : Sort.by("price").ascending();
        } else {
            sort = Sort.by("id"); // Default sort
        }

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        // ✅ Step 2: Filter active products
        List<Product> products = productPage.getContent().stream()
                .filter(p -> p.getIsActive() == ActiveStatus.ACTIVE)
                .toList();

        // ✅ Step 3: Filter by category name
        if (filter.getCategoryName() != null && !filter.getCategoryName().isBlank()) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null &&
                            p.getCategory().getCategoryName().equalsIgnoreCase(filter.getCategoryName()))
                    .toList();
        }

        // ✅ Step 4: Build response with inventory info
        List<ProductInventoryResponseDto> result = products.stream().map(p -> {
            Integer totalQty;
            if (factoryId != null) {
                totalQty = factoryInventoryRepository.findTotalQuantityByProductIdAndFactoryId(p.getId(), factoryId);
            } else {
                totalQty = factoryInventoryRepository.findTotalQuantityByProductId(p.getId());
            }

            int totalQuantity = (totalQty == null) ? 0 : totalQty;
            String stockStatus = totalQuantity > 0 ? "InStock" : "OutOfStock";

            return new ProductInventoryResponseDto(
                    p.getId(),
                    p.getName(),
                    p.getProductDescription(),
                    p.getPrice(),
                    p.getRewardPoint(),
                    p.getCategory().getCategoryName(),
                    p.getImage(),
                    p.getIsActive().name(),
                    totalQuantity,
                    stockStatus
            );
        }).toList();

        // ✅ Step 5: Filter by availability (InStock / OutOfStock)
        if (filter.getAvailability() != null && !filter.getAvailability().isBlank()) {
            if (filter.getAvailability().equalsIgnoreCase("InStock")) {
                result = result.stream().filter(r -> r.getTotalQuantity() > 0).toList();
            } else if (filter.getAvailability().equalsIgnoreCase("OutOfStock")) {
                result = result.stream().filter(r -> r.getTotalQuantity() == 0).toList();
            }
        }

        // ✅ Step 6: Manual sorting by quantity if requested
        if ("quantity".equalsIgnoreCase(filter.getSortBy())) {
            if ("desc".equalsIgnoreCase(filter.getSortDirection())) {
                result = result.stream()
                        .sorted(Comparator.comparing(ProductInventoryResponseDto::getTotalQuantity).reversed())
                        .toList();
            } else {
                result = result.stream()
                        .sorted(Comparator.comparing(ProductInventoryResponseDto::getTotalQuantity))
                        .toList();
            }
        }

        // ✅ Step 7: Pagination meta info
        Map<String, Object> pagination = PaginationUtil.build(productPage);

        return new ApiResponseDto<>(true, "Products fetched successfully", result, pagination);
    }






    /**
     * SOFT DELETE Product
     */
    @Transactional
    public ApiResponseDto<Void> deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(ActiveStatus.INACTIVE);
        productRepository.save(product);
        return new ApiResponseDto<>(true, "Product deleted successfully", null);
    }

    @Transactional
    public ApiResponseDto<CategoryResponseDto> updateCategory(Long id, CreateOrUpdateCategoryDto request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            category.setCategoryName(request.getCategoryName());
        }
        if (request.getCategoryDescription() != null && !request.getCategoryDescription().isBlank()) {
            category.setCategoryDescription(request.getCategoryDescription());
        }

        category.setUpdatedAt(LocalDateTime.now());
        ProductCategory saved = categoryRepository.save(category);

        CategoryResponseDto response = new CategoryResponseDto(
                saved.getId(),
                saved.getCategoryName(),
                saved.getCategoryDescription()
        );

        return new ApiResponseDto<>(true, "Category updated successfully", response);
    }

    /**
     * Permanently delete category from DB
     */
    @Transactional
    public ApiResponseDto<Void> deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        // ✅ Delete associated products first
        productRepository.deleteByCategory(category);

        // ✅ Now delete category
        categoryRepository.delete(category);

        return new ApiResponseDto<>(true, "Category and associated products deleted successfully", null);
    }

    /**
     * Normalizes a category name by trimming and capitalizing properly.
     * Example: " power tools " -> "Power Tools"
     */
    private String normalizeCategoryName(String name) {
        if (name == null) return null;
        name = name.trim().replaceAll("\\s+", " "); // Remove extra spaces between words

        // Capitalize first letter of each word
        return Arrays.stream(name.split(" "))
                .filter(word -> !word.isBlank())
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }


}
