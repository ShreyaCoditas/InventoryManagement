package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.service.CloudinaryService;
import com.inventory.inventorymanagementsystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CloudinaryService cloudinaryService;



    // CREATE
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> create(@Valid @ModelAttribute CreateProductDto dto, BindingResult result) {
        if (result.hasErrors()) {
            String msg = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, msg, null));
        }
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    // UPDATE
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> update(@PathVariable Long id, @ModelAttribute UpdateProductDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

//    @GetMapping("/getAllProducts")
//    public ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> getAllProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "id") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDir,
//            @RequestParam(required = false) List<String> categoryNames,
//            @RequestParam(required = false) String availability) {
//
//        ApiResponseDto<List<ProductResponseDto>> response =
//                productService.getAllProducts(page, size, sortBy, sortDir, categoryNames, availability);
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/getAllProducts")
    public ApiResponseDto<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) List<String> categoryNames,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status
    ) {
        return productService.getAllProducts(
                page,
                size,
                sortBy,
                sortDir,
                categoryNames,
                availability,
                search,status
        );
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/categories/all")
    public ResponseEntity<ApiResponseDto<List<ProductCategoryResponseDto>>> getCategoriesForProducts() {
        ApiResponseDto<List<ProductCategoryResponseDto>> response = productService.getAllProductCategories();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/categories/{id}/update")
    public ResponseEntity<ApiResponseDto<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateOrUpdateCategoryDto request) {

        ApiResponseDto<CategoryResponseDto> response = productService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/categories/{id}/delete")
    public ResponseEntity<ApiResponseDto<Void>> deleteCategory(@PathVariable Long id) {
        ApiResponseDto<Void> response = productService.deleteCategory(id);
        return ResponseEntity.ok(response);
    }



}
