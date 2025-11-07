package com.inventory.inventorymanagementsystem.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private Long id;
    private String username;
    private String email;
    private String token;
    private String role;

    public LoginResponseDto(String username) {
        this.username = username;
    }
}





//public ResponseDTO(String token) {
//    this.token = token;
//}