package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class UserListDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private ActiveStatus isActive;
    private LocalDateTime createdAt;
    private List<String> factoryNames;
}
