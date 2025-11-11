package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.Product;
import com.inventory.inventorymanagementsystem.entity.ProductCategory;
import com.inventory.inventorymanagementsystem.repository.FactoryInventoryRepository;
import com.inventory.inventorymanagementsystem.repository.ProductCategoryRepository;
import com.inventory.inventorymanagementsystem.repository.ProductRepository;
import com.inventory.inventorymanagementsystem.specifications.ProductSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    private CloudinaryService cloudinaryService;

//    @Transactional
//    public ApiResponseDto<ProductResponseDto> createOrUpdateProduct(CreateOrUpdateProductDto request, MultipartFile imageFile) {
//        try {
//            ProductCategory category = resolveCategory(request);
//            Product product = (request.getId() != null)
//                    ? productRepository.findById(request.getId())
//                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getId()))
//                    : new Product();
//
//            product.setName(request.getName());
//            product.setProductDescription(request.getProductDescription());
//            product.setCategory(category);
//            product.setPrice(request.getPrice());
//            product.setRewardPoint(calculateRewardPoints(request.getPrice()));
//            product.setIsActive(ActiveStatus.ACTIVE);
//
//            if (request.getId() == null)
//                product.setCreatedAt(LocalDateTime.now());
//            product.setUpdatedAt(LocalDateTime.now());
//            // Upload image only if new file is given
//            if (imageFile != null && !imageFile.isEmpty()) {
//                String uploadedUrl = cloudinaryService.uploadFile(imageFile);
//                product.setImage(uploadedUrl);
//            } else if (request.getImage() != null && !request.getImage().isBlank()) {
//                product.setImage(request.getImage());
//            }
//            Product saved = productRepository.save(product);
//            ProductResponseDto response = new ProductResponseDto(
//                    saved.getId(),
//                    saved.getName(),
//                    saved.getProductDescription(),
//                    saved.getPrice(),
//                    saved.getRewardPoint(),
//                    saved.getCategory().getCategoryName(),
//                    saved.getImage(),
//                    saved.getIsActive().name()
//            );
//            String msg = (request.getId() == null)
//                    ? "Product created successfully"
//                    : "Product updated successfully";
//            return new ApiResponseDto<>(true, msg, response);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ApiResponseDto<>(false, "Something went wrong: " + e.getMessage(), null);
//        }
//    }

//    @Transactional
//    public ApiResponseDto<ProductResponseDto> createOrUpdateProduct(
//            CreateOrUpdateProductDto dto, MultipartFile imageFile) {
//
//        try {
//            ProductCategory category = resolveCategory(dto);
//            Product product;
//
//            if (dto.getId() != null) {
//                // UPDATE
//                product = productRepository.findById(dto.getId())
//                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getId()));
//            } else {
//                // CREATE
//                product = new Product();
//                product.setCreatedAt(LocalDateTime.now());
//                product.setIsActive(ActiveStatus.ACTIVE);
//            }
//
//            product.setName(dto.getName().trim());
//            product.setProductDescription(dto.getProductDescription());
//            product.setCategory(category);
//            product.setPrice(dto.getPrice());
//            product.setRewardPoint(calculateRewardPoints(dto.getPrice()));
//            product.setUpdatedAt(LocalDateTime.now());
//
//            // HANDLE IMAGE
//            if (imageFile != null && !imageFile.isEmpty()) {
//                // Delete old image if exists
//                if (product.getImage() != null && product.getId() != null) {
//                    String publicId = cloudinaryService.extractPublicId(product.getImage());
//                    cloudinaryService.delete(publicId);
//                }
//                String newUrl = cloudinaryService.uploadFile(imageFile);
//                product.setImage(newUrl);
//            }
//            // Optional: allow setting image URL manually (e.g. from frontend)
//            else if (dto.getImage() != null && !dto.getImage().isBlank()) {
//                product.setImage(dto.getImage().trim());
//            }
//
//            Product saved = productRepository.save(product);
//
//            ProductResponseDto response = ProductResponseDto.builder()
//                    .id(saved.getId())
//                    .name(saved.getName())
//                    .productDescription(saved.getProductDescription())
//                    .price(saved.getPrice())
//                    .rewardPoint(saved.getRewardPoint())
//                    .categoryName(saved.getCategory().getCategoryName())
//                    .image(saved.getImage())
//                    .isActive(saved.getIsActive().name())
//                    .build();
//
//            String msg = dto.getId() == null ? "Product created" : "Product updated";
//            return new ApiResponseDto<>(true, msg, response);
//
//        } catch (Exception e) {
//            return new ApiResponseDto<>(false, "Error: " + e.getMessage(), null);
//        }
//    }

    public ApiResponseDto<ProductResponseDto> createOrUpdateProduct(
            CreateOrUpdateProductDto dto, MultipartFile imageFile) {

        try {
            ProductCategory category = resolveCategory(dto);
            Product product;

            boolean isCreate = dto.getId() == null;
            if (isCreate) {
                product = new Product();
                product.setCreatedAt(LocalDateTime.now());
                product.setIsActive(ActiveStatus.ACTIVE);
            } else {
                product = productRepository.findById(dto.getId())
                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + dto.getId()));
            }

            // Only update fields that are provided
            product.setName(dto.getName().trim());
            product.setProductDescription(dto.getProductDescription());
            product.setPrice(dto.getPrice());
            product.setRewardPoint(calculateRewardPoints(dto.getPrice()));
            product.setCategory(category);
            product.setUpdatedAt(LocalDateTime.now());

            // IMAGE LOGIC
            if (imageFile != null && !imageFile.isEmpty()) {
                // Delete old image
                if (!isCreate && product.getImage() != null) {
                    String publicId = cloudinaryService.extractPublicId(product.getImage());
                    cloudinaryService.delete(publicId);
                }
                String url = cloudinaryService.uploadFile(imageFile);
                product.setImage(url);
            }
            // Allow manual image URL (optional)
            else if (dto.getImage() != null && !dto.getImage().isBlank()) {
                product.setImage(dto.getImage().trim());
            }
            // If no image and it's create → required
            else if (isCreate) {
                return new ApiResponseDto<>(false, "Image is required for new product", null);
            }

            Product saved = productRepository.save(product);

            ProductResponseDto response = ProductResponseDto.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .productDescription(saved.getProductDescription())
                    .price(saved.getPrice())
                    .rewardPoint(saved.getRewardPoint())
                    .categoryName(saved.getCategory().getCategoryName())
                    .image(saved.getImage())
                    .isActive(saved.getIsActive().name())
                    .build();

            String msg = isCreate ? "Product created successfully" : "Product updated successfully";
            return new ApiResponseDto<>(true, msg, response);

        } catch (Exception e) {
            return new ApiResponseDto<>(false, "Error: " + e.getMessage(), null);
        }
    }


    public ApiResponseDto<List<ProductInventoryResponseDto>> getAllProducts(ProductFilterSortDto filter, Long factoryId, Long productId
    ) {
        Specification<Product> spec = ProductSpecifications.withFilters(
                filter.getCategoryName(),
                filter.getAvailability(),
                filter.getStatus(),
                productId
        );
        Sort sort;
        if ("quantity".equalsIgnoreCase(filter.getSortBy())) {
            sort = Sort.by("id"); // will manually sort later
        } else if ("price".equalsIgnoreCase(filter.getSortBy())) {
            sort = "desc".equalsIgnoreCase(filter.getSortDirection())
                    ? Sort.by("price").descending()
                    : Sort.by("price").ascending();
        } else {
            sort = Sort.by("id");
        }
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<Product> products = productPage.getContent();
        List<ProductInventoryResponseDto> result = products.stream().map(p -> {
            Integer totalQty = (factoryId != null)
                    ? factoryInventoryRepository.findTotalQuantityByProductIdAndFactoryId(p.getId(), factoryId)
                    : factoryInventoryRepository.findTotalQuantityByProductId(p.getId());

            int totalQuantity = totalQty != null ? totalQty : 0;
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

        Map<String, Object> pagination = PaginationUtil.build(productPage);
        return new ApiResponseDto<>(true, "Products fetched successfully", result, pagination);
    }


//    public ApiResponseDto<List<ProductInventoryResponseDto>> getAllProducts(
//            ProductFilterSortDto filter, Long factoryId) {
//        Sort sort;
//        if ("quantity".equalsIgnoreCase(filter.getSortBy())) {
//            // Manual sort later for quantity
//            sort = Sort.by("id");
//        } else if ("price".equalsIgnoreCase(filter.getSortBy())) {
//            sort = "desc".equalsIgnoreCase(filter.getSortDirection())
//                    ? Sort.by("price").descending()
//                    : Sort.by("price").ascending();
//        } else {
//            sort = Sort.by("id"); // Default sort
//        }
//        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
//        Page<Product> productPage = productRepository.findAll(pageable);
//
//        // Step 2: Filter active products
//        List<Product> products = productPage.getContent().stream()
//                .filter(p -> p.getIsActive() == ActiveStatus.ACTIVE)
//                .toList();
//
//        // Step 3: Filter by category name
//        if (filter.getCategoryName() != null && !filter.getCategoryName().isBlank()) {
//            products = products.stream()
//                    .filter(p -> p.getCategory() != null &&
//                            p.getCategory().getCategoryName().equalsIgnoreCase(filter.getCategoryName()))
//                    .toList();
//        }
//
//        // Step 4: Build response with inventory info
//        List<ProductInventoryResponseDto> result = products.stream().map(p -> {
//            Integer totalQty;
//            if (factoryId != null) {
//                totalQty = factoryInventoryRepository.findTotalQuantityByProductIdAndFactoryId(p.getId(), factoryId);
//            } else {
//                totalQty = factoryInventoryRepository.findTotalQuantityByProductId(p.getId());
//            }
//
//            int totalQuantity = (totalQty == null) ? 0 : totalQty;
//            String stockStatus = totalQuantity > 0 ? "InStock" : "OutOfStock";
//
//            return new ProductInventoryResponseDto(
//                    p.getId(),
//                    p.getName(),
//                    p.getProductDescription(),
//                    p.getPrice(),
//                    p.getRewardPoint(),
//                    p.getCategory().getCategoryName(),
//                    p.getImage(),
//                    p.getIsActive().name(),
//                    totalQuantity,
//                    stockStatus
//            );
//        }).toList();
//
//        // Step 5: Filter by availability (InStock / OutOfStock)
//        if (filter.getAvailability() != null && !filter.getAvailability().isBlank()) {
//            if (filter.getAvailability().equalsIgnoreCase("InStock")) {
//                result = result.stream().filter(r -> r.getTotalQuantity() > 0).toList();
//            } else if (filter.getAvailability().equalsIgnoreCase("OutOfStock")) {
//                result = result.stream().filter(r -> r.getTotalQuantity() == 0).toList();
//            }
//        }
//
//        // Step 6: Manual sorting by quantity if requested
//        if ("quantity".equalsIgnoreCase(filter.getSortBy())) {
//            if ("desc".equalsIgnoreCase(filter.getSortDirection())) {
//                result = result.stream()
//                        .sorted(Comparator.comparing(ProductInventoryResponseDto::getTotalQuantity).reversed())
//                        .toList();
//            } else {
//                result = result.stream()
//                        .sorted(Comparator.comparing(ProductInventoryResponseDto::getTotalQuantity))
//                        .toList();
//            }
//        }
//
//        // Step 7: Pagination meta info
//        Map<String, Object> pagination = PaginationUtil.build(productPage);
//        return new ApiResponseDto<>(true, "Products fetched successfully", result, pagination);
//    }


    @Transactional
    public ApiResponseDto<Void> deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(ActiveStatus.INACTIVE);
        productRepository.save(product);
        return new ApiResponseDto<>(true, "Product deleted successfully", null);
    }


    public List<String> getActiveCategoryNames() {
        return categoryRepository.findAll()
                .stream()
                .map(ProductCategory::getCategoryName)
                .sorted()  // Alphabetical order
                .toList();
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

    @Transactional
    public ApiResponseDto<Void> deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        productRepository.deleteByCategory(category);
        categoryRepository.delete(category);
        return new ApiResponseDto<>(true, "Category and associated products deleted successfully", null);
    }




    private int calculateRewardPoints(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.001))
                .setScale(0, BigDecimal.ROUND_CEILING)
                .intValue();
    }

    private ProductCategory resolveCategory(CreateOrUpdateProductDto request) {
        if (request.getCategoryId() != null) {
            return categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Invalid category ID: " + request.getCategoryId()));
        }
        if (request.getNewCategoryName() != null && !request.getNewCategoryName().isBlank()) {
            return categoryRepository.findByCategoryNameIgnoreCase(request.getNewCategoryName())
                    .orElseGet(() -> {
                        ProductCategory newCat = new ProductCategory();
                        newCat.setCategoryName(request.getNewCategoryName().trim());
                        newCat.setCategoryDescription("Auto-created category");
                        newCat.setCreatedAt(LocalDateTime.now());
                        newCat.setUpdatedAt(LocalDateTime.now());
                        return categoryRepository.save(newCat);
                    });
        }
        throw new RuntimeException("Either categoryId or newCategoryName must be provided");
    }

}
