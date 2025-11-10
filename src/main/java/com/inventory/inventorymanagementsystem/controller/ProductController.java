package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.service.CloudinaryService;
import com.inventory.inventorymanagementsystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/create-product")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createOrUpdateProduct(
            @Valid @RequestBody CreateOrUpdateProductDto request) {
        return ResponseEntity.ok(productService.createOrUpdateProduct(request));
    }

    @GetMapping("/allproducts")
    public ResponseEntity<ApiResponseDto<List<ProductInventoryResponseDto>>> getAllProducts(
            @RequestParam(required = false) Long factoryId,   // ✅ Optional factory ID
            @ModelAttribute ProductFilterSortDto filter) {

        ApiResponseDto<List<ProductInventoryResponseDto>> response =
                productService.getAllProducts(filter, factoryId);  // ✅ Pass to service

        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @PutMapping("/categories/{id}/update")
    public ResponseEntity<ApiResponseDto<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @RequestBody CreateOrUpdateCategoryDto request) {

        ApiResponseDto<CategoryResponseDto> response = productService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/categories/{id}/delete")
    public ResponseEntity<ApiResponseDto<Void>> deleteCategory(@PathVariable Long id) {
        ApiResponseDto<Void> response = productService.deleteCategory(id);
        return ResponseEntity.ok(response);
    }



}
