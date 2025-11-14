package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> createTool(
            @Valid @ModelAttribute ToolDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            String msg = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, msg, null));
        }

        return ResponseEntity.ok(toolService.createTool(dto));
    }

    @PutMapping(value = "/update/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> updateTool(
            @PathVariable Long id,
            @Valid @ModelAttribute UpdateToolDto dto,
            BindingResult result) {

        if (result.hasErrors()) {
            String msg = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiResponseDto<>(false, msg, null));
        }

        return ResponseEntity.ok(toolService.updateTool(id, dto));
    }


    @GetMapping("/getAll")
    public ResponseEntity<ApiResponseDto<List<ToolResponseDto>>> getAllTools(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) List<String> categoryNames) {

        ApiResponseDto<List<ToolResponseDto>> response =
                toolService.getAllTools(page, size, sortBy, sortDir, availability, factoryId, categoryNames);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> getToolById(@PathVariable Long id) {
        return ResponseEntity.ok(toolService.getToolById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDto<String>> softDeleteTool(@PathVariable Long id) {
        return ResponseEntity.ok(toolService.softDeleteTool(id));
    }

    @GetMapping("/category/all")
    public ResponseEntity<ApiResponseDto<List<ToolCategoryResponseDto>>> getAll() {
        return ResponseEntity.ok(toolService.getAllCategories());
    }

    @PutMapping("category/update/{id}")
    public ResponseEntity<ApiResponseDto<ToolCategoryResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ToolCategoryRequestDto dto) {
        return ResponseEntity.ok(toolService.updateCategory(id, dto));
    }

    @DeleteMapping("/category/delete/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(toolService.deleteCategory(id));
    }



    @PostMapping("/stock/add")
    public ResponseEntity<ApiResponseDto<String>> addToolStock(
            @RequestBody AddToolStockDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ApiResponseDto<String> response = toolService.addToolStock(dto, currentUser);
        return ResponseEntity.ok(response);
    }

}