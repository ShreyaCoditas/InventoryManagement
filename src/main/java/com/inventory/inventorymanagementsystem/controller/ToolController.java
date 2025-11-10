package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    // =====================================================
    // ✅ TOOL CRUD ENDPOINTS
    // =====================================================

    /**
     * Create a new tool
     */
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponseDto<ToolResponseDto>> createTool( @Valid @RequestBody ToolRequestDto dto) {
//        ApiResponseDto<ToolResponseDto> response = toolService.createTool(dto);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Update an existing tool
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> updateTool(
            @PathVariable Long id,
            @Valid @RequestBody UpdateToolDto dto) {

        ApiResponseDto<ToolResponseDto> response = toolService.updateTool(id, dto);
        return ResponseEntity.ok(response);
    }


    /**
     * Soft delete a tool
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDto<String>> softDeleteTool(@PathVariable Long id) {
        ApiResponseDto<String> response = toolService.softDeleteTool(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tools (paginated and sortable)
     */
    @GetMapping("/alltools")
    public ResponseEntity<ApiResponseDto<List<ToolResponseDto>>> getAllTools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        ApiResponseDto<List<ToolResponseDto>> response =
                toolService.getAllTools(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    // =====================================================
    // ✅ TOOL CATEGORY ENDPOINTS (Under /api/tools/categories)
    // =====================================================

    /**
     * Get all  tool categories (for dropdown)
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponseDto<List<ToolCategoryResponseDto>>> getAllCategories() {
        ApiResponseDto<List<ToolCategoryResponseDto>> response =
                toolService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    /**
     * Update tool category
     */
    @PutMapping("/categories/edit/{id}")
    public ResponseEntity<ApiResponseDto<ToolCategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @RequestBody ToolCategoryRequestDto dto) {
        ApiResponseDto<ToolCategoryResponseDto> response =
                toolService.updateCategory(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft delete tool category
     */
    @DeleteMapping("/categories/delete/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteCategory(@PathVariable Long id) {
        ApiResponseDto<String> response = toolService.deleteCategory(id);
        return ResponseEntity.ok(response);
    }
}
