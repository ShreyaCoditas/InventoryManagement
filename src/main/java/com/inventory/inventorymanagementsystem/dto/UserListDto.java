package com.inventory.inventorymanagementsystem.dto;



import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListDto {
    private Long id;
    private String username;
    private String email;
//    private Role role;
//    private Account_Status isActive;
private RoleName role;

    // ActiveStatus enum (YES / NO)
    private ActiveStatus isActive;
}



