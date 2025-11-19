package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.Product;
import com.inventory.inventorymanagementsystem.entity.ProductCategory;
import com.inventory.inventorymanagementsystem.exceptions.CustomException;
import com.inventory.inventorymanagementsystem.exceptions.ResourceNotFoundException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private FactoryInventoryRepository factoryInventoryRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    // CREATE
    public ApiResponseDto<ProductResponseDto> createProduct(CreateProductDto dto) {
        validateCategory(dto.getCategoryId(), dto.getNewCategoryName());

        if (productRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            return new ApiResponseDto<>(false, "Product already exists", null);
        }

        String imageUrl = cloudinaryService.uploadFile(dto.getImageFile());
        ProductCategory category = resolveCategory(dto.getCategoryId(), dto.getNewCategoryName());

        Product product = new Product();
        product.setName(dto.getName().trim());
        product.setProductDescription(dto.getProductDescription());
        product.setPrice(dto.getPrice());
        product.setRewardPoint(calculateRewardPoints(dto.getPrice()));
        product.setCategory(category);
        product.setImage(imageUrl);
        product.setIsActive(ActiveStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return saveAndRespond(product, "Product created");
    }


    // UPDATE
    public ApiResponseDto<ProductResponseDto> updateProduct(Long id, UpdateProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            String name = dto.getName().trim();
            if (!name.equalsIgnoreCase(product.getName()) && productRepository.existsByNameIgnoreCase(name)) {
                return new ApiResponseDto<>(false, "Name already taken", null);
            }
            product.setName(name);
        }
        if (dto.getProductDescription() != null && !dto.getProductDescription().isBlank()) {
            product.setProductDescription(dto.getProductDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
            product.setRewardPoint(calculateRewardPoints(dto.getPrice()));
        }
        if (dto.getCategoryId() != null || (dto.getNewCategoryName() != null && !dto.getNewCategoryName().isBlank())) {
            validateCategory(dto.getCategoryId(), dto.getNewCategoryName());
            product.setCategory(resolveCategory(dto.getCategoryId(), dto.getNewCategoryName()));
        }
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            if (product.getImage() != null) {
                cloudinaryService.delete(cloudinaryService.extractPublicId(product.getImage()));
            }
            product.setImage(cloudinaryService.uploadFile(dto.getImage()));
        }
        product.setUpdatedAt(LocalDateTime.now());
        return saveAndRespond(product, "Product updated");
    }


    @Transactional
    public ApiResponseDto<Void> deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setIsActive(ActiveStatus.INACTIVE);
        productRepository.save(product);
        return new ApiResponseDto<>(true, "Product deleted successfully", null);
    }

    @Transactional
    public ApiResponseDto<List<ProductResponseDto>> getAllProducts(int page, int size, String sortBy,
            String sortDir,
            List<String> categoryNames,
            String availability,
            String search,
            List<String> status
    ) {

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }

        Pageable pageable;

        if (sortBy.equalsIgnoreCase("quantity")) {
            pageable = PageRequest.of(page, size);
        } else {
            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            pageable = PageRequest.of(page, size, sort);
        }

        Specification<Product> spec = Specification
                .allOf(ProductSpecifications.hasStatuses(status))
                .and(ProductSpecifications.search(search))
                .and(ProductSpecifications.inCategories(categoryNames))
                .and(ProductSpecifications.availability(availability));

        if (sortBy.equalsIgnoreCase("quantity")) {
            spec = spec.and(ProductSpecifications.sortByQuantity(sortDir));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<Long> ids = productPage.stream().map(Product::getId).toList();
        Map<Long, Integer> quantities = factoryInventoryRepository.findQuantitiesForProducts(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        List<ProductResponseDto> dtos = productPage.getContent().stream()
                .map(p -> {
                    int qty = quantities.getOrDefault(p.getId(), 0);

                    return ProductResponseDto.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : null)
                            .price(p.getPrice())
                            .rewardPoint(p.getRewardPoint())
                            .quantity(qty)
                            .productDescription(p.getProductDescription())
                            .image(p.getImage())
                            .isActive(p.getIsActive().name())
                            .StockStatus(qty > 0 ? "INSTOCK" : "OUTOFSTOCK")
                            .build();
                })
                .toList();

        return new ApiResponseDto<>(true, "Products fetched successfully", dtos, PaginationUtil.build(productPage));
    }





    @Transactional
    public ApiResponseDto<CategoryResponseDto> updateCategory(Long id, CreateOrUpdateCategoryDto request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
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
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        productRepository.deleteByCategory(category);
        categoryRepository.delete(category);
        return new ApiResponseDto<>(true, "Category and associated products deleted successfully", null);
    }

    public ApiResponseDto<List<ProductCategoryResponseDto>> getAllProductCategories() {
        List<ProductCategory> categories = categoryRepository.findAll();

        List<ProductCategoryResponseDto> categoryDtos = categories.stream()
                .map(cat -> ProductCategoryResponseDto.builder()
                        .categoryId(cat.getId())
                        .categoryName(cat.getCategoryName())
                        .build())
                .toList();

        return new ApiResponseDto<>(true, "Product categories fetched successfully", categoryDtos);
    }




    // REUSABLE
    private void validateCategory(Long categoryId, String newCategoryName) {
        boolean hasId = categoryId != null && categoryId > 0;
        boolean hasName = newCategoryName != null && !newCategoryName.isBlank();
        if (!hasId && !hasName) {
            throw new CustomException("Either categoryId or newCategoryName is required", HttpStatus.BAD_REQUEST);
        }
        if (hasId && hasName) {
            throw new CustomException("Only one of categoryId or newCategoryName should be provided",HttpStatus.BAD_REQUEST);
        }
    }

    private ProductCategory resolveCategory(Long categoryId, String newCategoryName) {
        if (categoryId != null) {
            return categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Invalid category ID"));
        }
        return categoryRepository.findByCategoryNameIgnoreCase(newCategoryName.trim())
                .orElseGet(() -> {
                    ProductCategory cat = new ProductCategory();
                    cat.setCategoryName(newCategoryName.trim());
                    cat.setCategoryDescription("Auto-created");
                    cat.setCreatedAt(LocalDateTime.now());
                    cat.setUpdatedAt(LocalDateTime.now());
                    return categoryRepository.save(cat);
                });
    }

    private ApiResponseDto<ProductResponseDto> saveAndRespond(Product p, String msg) {
        Product saved = productRepository.save(p);
        return new ApiResponseDto<>(true, msg, toResponseDto(saved));
    }

    private ProductResponseDto toResponseDto(Product p) {
        return ProductResponseDto.builder()
                .id(p.getId())
                .name(p.getName())
                .productDescription(p.getProductDescription())
                .price(p.getPrice())
                .rewardPoint(p.getRewardPoint())
                .categoryName(p.getCategory().getCategoryName())
                .image(p.getImage())
                .isActive(p.getIsActive().name())
//                .StockStatus(p.get)
                .build();
    }

    private int calculateRewardPoints(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(0.001))
                .setScale(0, BigDecimal.ROUND_CEILING)
                .intValue();
    }
    }


