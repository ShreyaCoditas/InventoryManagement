//package com.inventory.inventorymanagementsystem.controller;
//
//import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
//import com.inventory.inventorymanagementsystem.dto.CreatePlantHeadRequestDto;
//import com.inventory.inventorymanagementsystem.dto.PlantHeadResponseDto;
//import com.inventory.inventorymanagementsystem.entity.User;
//import com.inventory.inventorymanagementsystem.security.UserPrincipal;
//import com.inventory.inventorymanagementsystem.service.PlantHeadService;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/plantheads")
//@RequiredArgsConstructor
//public class PlantHeadController {
//
//    @Autowired
//    private  PlantHeadService plantHeadService;
//
//    @PreAuthorize("hasRole('OWNER')")
//    @PostMapping("/create")
//    //@Operation(summary = "Create a new PlantHead (Only Owner)")
//    //@ApiResponses({@ApiResponse(responseCode = "200", description = "PlantHead created successfully")})
//    public ResponseEntity<ApiResponseDto<PlantHeadResponseDto>> createPlantHead(
//            @RequestBody CreatePlantHeadRequestDto request,
//            @AuthenticationPrincipal UserPrincipal userPrincipal
//    ) {
//        User owner = userPrincipal.getUser();
//        PlantHeadResponseDto created = plantHeadService.createPlantHead(request, owner);
//        return ResponseEntity.ok(new ApiResponseDto<>(true, "PlantHead created successfully", created));
//    }
//}
