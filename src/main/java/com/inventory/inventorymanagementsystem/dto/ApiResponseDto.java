package com.inventory.inventorymanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> pagination;

    public ApiResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponseDto(boolean success, String message, T data, Map<String, Object> pagination) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.pagination = pagination;
    }
}
