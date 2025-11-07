//package com.inventory.inventorymanagementsystem.controller;
//
//import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
//import com.inventory.inventorymanagementsystem.dto.CreateFactoryRequestDto;
//import com.inventory.inventorymanagementsystem.dto.FactoryResponseDto;
//import com.inventory.inventorymanagementsystem.entity.User;
//import com.inventory.inventorymanagementsystem.security.UserPrincipal;
//import com.inventory.inventorymanagementsystem.service.FactoryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/factories")
//@RequiredArgsConstructor
//public class FactoryController {
//
//    private final FactoryService factoryService;
//
//    @PreAuthorize("hasRole('OWNER')")
//    @PostMapping("/create")
////    @Operation(summary = "Create a new factory (Only Owner)")
////    @ApiResponses({@ApiResponse(responseCode = "200", description = "Factory created successfully")})
//    public ResponseEntity<ApiResponseDto<FactoryResponseDto>> createFactory(
//            @RequestBody CreateFactoryRequestDto request,
//            @AuthenticationPrincipal UserPrincipal userPrincipal
//    ) {
//        User owner = userPrincipal.getUser();
//        FactoryResponseDto factoryResponse = factoryService.createFactory(request, owner);
//        return ResponseEntity.ok(new ApiResponseDto<>(true, "Factory created successfully", factoryResponse));
//    }
//
////    @GetMapping("/factories")
////    public ResponseEntity<List<factorySummaryDto>> getAllFactories () {
////        return ResponseEntity.ok(factoryService.getAllFactories());
////    }
////
////    @PostMapping("/createFactory")
////    public ResponseEntity<factoryCreationResponseDTO> createFactory (@Valid @RequestBody factoryCreationDTO factoryCreationDTO) {
////        return factoryService.createFactory(factoryCreationDTO);
////    }
////
//////    @GetMapping("/factories/{factory_id}")
//////    public ResponseEntity<List<factoryDetailsDTO>> getFactory (@PathVariable Long factory_id) {
//////        return ResponseEntity.ok(factoryService.getFactoryById(factory_id));
//////    }
////
////    @DeleteMapping("/deleteFactory/{id}")
////    public ResponseEntity<factoryDeletionMsgDTO> deleteFactory (@PathVariable Long id) {
////        return factoryService.deleteFactory(id);
////    }
//
//}
//
//
