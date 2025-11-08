package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.FactoryInventoryStock;
import com.inventory.inventorymanagementsystem.entity.Product;
import com.inventory.inventorymanagementsystem.entity.ProductCategory;
import com.inventory.inventorymanagementsystem.repository.FactoryInventoryRepository;
import com.inventory.inventorymanagementsystem.repository.ProductCategoryRepository;
import com.inventory.inventorymanagementsystem.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service

public class ProductService {

    @Autowired
    private  ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private FactoryInventoryRepository factoryInventoryRepository;


    /**
     * CREATE or UPDATE Product
     */
    @Transactional
    public ApiResponseDto<ProductResponseDto> createOrUpdateProduct(CreateOrUpdateProductDto request) {

        // ✅ Step 1: Find or create category
        ProductCategory category = null;

        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Invalid category ID"));
        } else if (request.getNewCategoryName() != null && !request.getNewCategoryName().isBlank()) {
            category = categoryRepository.findByCategoryNameIgnoreCase(request.getNewCategoryName())
                    .orElseGet(() -> {
                        ProductCategory newCat = new ProductCategory();
                        newCat.setCategoryName(request.getNewCategoryName());
                        newCat.setCategoryDescription("Auto-created from product form");
                        newCat.setCreatedAt(LocalDateTime.now());
                        newCat.setUpdatedAt(LocalDateTime.now());
                        return categoryRepository.save(newCat);
                    });
        } else {
            throw new RuntimeException("Either categoryId or newCategoryName must be provided");
        }

        Product product;

        // ✅ Step 2: Create or update logic
        if (request.getId() != null) {
            // --- UPDATE EXISTING PRODUCT ---
            product = productRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (request.getName() != null) product.setName(request.getName());
            if (request.getProductDescription() != null) product.setProductDescription(request.getProductDescription());
            if (request.getPrice() != null) product.setPrice(request.getPrice());
            if (request.getImage() != null) product.setImage(request.getImage());
            if (category != null) product.setCategory(category);

            // ✅ Recalculate reward points
            product.setRewardPoint(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(0.001))
                            .setScale(0, BigDecimal.ROUND_CEILING)
                            .intValue()
            );

            product.setUpdatedAt(LocalDateTime.now());

        } else {
            // --- CREATE NEW PRODUCT ---
            product = new Product();
            product.setName(request.getName());
            product.setProductDescription(request.getProductDescription());
            product.setCategory(category);
            product.setPrice(request.getPrice());
            product.setRewardPoint(
                    request.getPrice()
                            .multiply(BigDecimal.valueOf(0.001))
                            .setScale(0, BigDecimal.ROUND_CEILING)
                            .intValue()
            );

            product.setImage(request.getImage());
            product.setIsActive(ActiveStatus.YES);
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
        }

        // ✅ Save Product
        Product saved = productRepository.save(product);

        // ✅ Build Response DTO
        ProductResponseDto response = new ProductResponseDto(
                saved.getId(),
                saved.getName(),
                saved.getProductDescription(),
                saved.getPrice(),
                saved.getRewardPoint(),
                saved.getCategory().getCategoryName(),
                saved.getImage(),
                saved.getIsActive().name()
        );

        return new ApiResponseDto<>(
                true,
                request.getId() == null ? "Product created successfully" : "Product updated successfully",
                response
        );
    }

    /**
     * GET ALL Products with filter + sort
     */
    public ApiResponseDto<List<ProductResponseDto>> getAllProducts(ProductFilterSortDto filter) {

        List<Product> products = productRepository.findAll().stream()
                .filter(p -> p.getIsActive() == ActiveStatus.YES)
                .toList();

        // ✅ Filter by category if needed
        if (filter.getCategoryName() != null && !filter.getCategoryName().isBlank()) {
            products = products.stream()
                    .filter(p -> p.getCategory().getCategoryName()
                            .equalsIgnoreCase(filter.getCategoryName()))
                    .toList();
        }

        // ✅ Sort logic (optional)
        if (filter.getSortBy() != null) {
            switch (filter.getSortBy()) {
                case "priceLowHigh" ->
                        products = products.stream().sorted(Comparator.comparing(Product::getPrice)).toList();
                case "priceHighLow" ->
                        products = products.stream().sorted(Comparator.comparing(Product::getPrice).reversed()).toList();
            }
        }

        // ✅ Now join inventory stock quantities
        List<ProductResponseDto> result = products.stream().map(p -> {
            // Sum quantity from factories_inventory_stock table
            Integer totalQty = factoryInventoryRepository.findTotalQuantityByProductId(p.getId());

            // Handle null (if no stock entry exists yet)
            int totalQuantity = (totalQty == null) ? 0 : totalQty;

            // Decide stock status
            String stockStatus = totalQuantity > 0 ? "In Stock" : "Out of Stock";

            return new ProductResponseDto(
                    p.getId(),
                    p.getName(),
                    p.getProductDescription(),
                    p.getPrice(),
                    p.getRewardPoint(),
                    p.getCategory().getCategoryName(),
                    p.getImage(),
                    p.getIsActive().name(),
                    totalQuantity,      // new field
                    stockStatus         // new field
            );
        }).toList();

        return new ApiResponseDto<>(true, "Products fetched successfully", result);
    }




    /**
     * SOFT DELETE Product
     */
    @Transactional
    public ApiResponseDto<Void> deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(ActiveStatus.NO);
        productRepository.save(product);
        return new ApiResponseDto<>(true, "Product deleted successfully", null);
    }
}
