package com.inventory.inventorymanagementsystem.exceptions;

import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors from @Valid annotated DTOs.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        String errorMessage = "Validation failed: " + errors.entrySet().stream()
                .map(e -> e.getKey() + " " + e.getValue())
                .collect(Collectors.joining(", "));

        ApiResponseDto<Object> response = new ApiResponseDto<>(false, errorMessage, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.NOT_FOUND.value());

        ApiResponseDto<Map<String, Object>> response = new ApiResponseDto<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());

        ApiResponseDto<Map<String, Object>> response = new ApiResponseDto<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());

        ApiResponseDto<Map<String, Object>> response = new ApiResponseDto<>(false, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDto<Object>> handleCustomException(CustomException ex) {
        ApiResponseDto<Object> response = new ApiResponseDto<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(response, ex.getStatus()); // ✅ status code appears in Postman
    }

        // Handles known runtime exceptions from service/business logic (like "User not found").
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponseDto<Map<String,Object>>> handleRuntimeExceptions(RuntimeException ex) {
            Map<String,Object> errors=new HashMap<>();
            errors.put("error",ex.getMessage());
            errors.put("timestamp",LocalDateTime.now());
            errors.put("status",HttpStatus.BAD_REQUEST.value());
            ApiResponseDto<Map<String,Object>> response = new ApiResponseDto<>(false, ex.getMessage(),null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }




        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponseDto<String>> handleAllExceptions(Exception ex) {
            ApiResponseDto<String> response =
                    new ApiResponseDto<>(false, "Something went wrong: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }






}

