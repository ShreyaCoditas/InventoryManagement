package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChiefSupervisorRequestDto {
    private String name;
    private String email;
    private String phoneNumber;
    private Long factoryId;   // ✅ Required - owner or planthead selects factory

}

