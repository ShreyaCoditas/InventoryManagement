package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.LoginRequestDto;
import com.inventory.inventorymanagementsystem.dto.LoginResponseDto;
import com.inventory.inventorymanagementsystem.dto.DistributorRegisterDto;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    //Register API
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<Void>> registerDistributor(
            @Valid @RequestBody DistributorRegisterDto dto) {

        userService.registerDistributor(dto);

        ApiResponseDto<Void> response = new ApiResponseDto<>(
                true,
                "Distributor registered successfully!"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto userDto) {
        LoginResponseDto loginResponseDTO = userService.login(userDto);
        ApiResponseDto<LoginResponseDto> apiResponseDTO = new ApiResponseDto<>(true, "Login successful", loginResponseDTO);
        return ResponseEntity.ok(apiResponseDTO);
    }
}

