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


//    @PostMapping(value = "/createorupdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createOrUpdateProduct(
//            @ModelAttribute CreateOrUpdateProductDto request,
//            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
//        ApiResponseDto<ProductResponseDto> response = productService.createOrUpdateProduct(request, imageFile);
//        return ResponseEntity.ok(response);
//    }

//    @PostMapping(value = "/createorupdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createOrUpdateProduct(
//            @Valid @ModelAttribute CreateOrUpdateProductDto request,
//            BindingResult result,
//            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
//
//        if (result.hasErrors()) {
//            String msg = result.getFieldErrors().get(0).getDefaultMessage();
//            return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, msg, null));
//        }
//
//        ApiResponseDto<ProductResponseDto> response = productService.createOrUpdateProduct(request, imageFile);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping(value = "/createorupdate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createOrUpdateProduct(
            @Valid @ModelAttribute CreateOrUpdateProductDto dto,
            BindingResult result,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        if (result.hasErrors()) {
            String msg = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, msg, null));
        }

        ApiResponseDto<ProductResponseDto> response = productService.createOrUpdateProduct(dto, imageFile);
        return ResponseEntity.ok(response);
    }
//    @GetMapping("/allproducts")
//    public ResponseEntity<ApiResponseDto<List<ProductInventoryResponseDto>>> getAllProducts(
//            @RequestParam(required = false) Long factoryId,   // Optional factory ID
//            @ModelAttribute ProductFilterSortDto filter) {
//        ApiResponseDto<List<ProductInventoryResponseDto>> response =
//                productService.getAllProducts(filter, factoryId);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/allproducts")
    public ResponseEntity<ApiResponseDto<List<ProductInventoryResponseDto>>> getAllProducts(
            @ModelAttribute ProductFilterSortDto filter,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) Long productId
    ) {
        ApiResponseDto<List<ProductInventoryResponseDto>> response = productService.getAllProducts(filter, factoryId, productId);
        return ResponseEntity.ok(response);
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping("/categories/all")
    public ResponseEntity<ApiResponseDto<List<String>>> getAllCategoryNames() {
        List<String> categoryNames = productService.getActiveCategoryNames();
        return ResponseEntity.ok(
                new ApiResponseDto<>(true, "Category names fetched", categoryNames)
        );
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
