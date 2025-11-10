package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.MerchandiseRequestDto;
import com.inventory.inventorymanagementsystem.dto.MerchandiseResponseDto;
import com.inventory.inventorymanagementsystem.service.MerchandiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchandise")
public class MerchandiseController {

    @Autowired
    private MerchandiseService ownerService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> createMerchandise(
            @RequestBody MerchandiseRequestDto request) {
        ApiResponseDto<MerchandiseResponseDto> response = ownerService.createMerchandise(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> updateMerchandise(
            @PathVariable Long id,
            @RequestBody MerchandiseRequestDto request) {

        ApiResponseDto<MerchandiseResponseDto> response = ownerService.updateMerchandise(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDto<String>> softDeleteMerchandise(@PathVariable Long id) {

        ApiResponseDto<String> response = ownerService.softDeleteMerchandise(id);
        return ResponseEntity.ok(response);
    }
}
