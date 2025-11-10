package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/profile")
public class UserController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload-image")
    public ResponseEntity<ApiResponseDto<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = cloudinaryService.uploadFile(file);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Image uploaded successfully", imageUrl));
    }
}
