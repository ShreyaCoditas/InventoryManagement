//package com.inventory.inventorymanagementsystem.controller;
//
//import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
//import com.inventory.inventorymanagementsystem.dto.AddMerchandiseDto;
//import com.inventory.inventorymanagementsystem.dto.MerchandiseResponseDto;
//import com.inventory.inventorymanagementsystem.service.MerchandiseService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/merchandise")
//public class MerchandiseController {
//
//    @Autowired
//    private MerchandiseService ownerService;
//
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> createMerchandise(
//            @RequestBody AddMerchandiseDto request) {
//        ApiResponseDto<MerchandiseResponseDto> response = ownerService.createMerchandise(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/update/{id}")
//    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> updateMerchandise(
//            @PathVariable Long id,
//            @RequestBody AddMerchandiseDto request) {
//
//        ApiResponseDto<MerchandiseResponseDto> response = ownerService.updateMerchandise(id, request);
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<ApiResponseDto<String>> softDeleteMerchandise(@PathVariable Long id) {
//
//        ApiResponseDto<String> response = ownerService.softDeleteMerchandise(id);
//        return ResponseEntity.ok(response);
//    }
////
////    @PostMapping(value = "/add/merchandise", consumes = {"multipart/form-data"})
////    @PreAuthorize("hasAnyAuthority('OWNER', 'CENTRAL_OFFICE')")
////    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> addMerchandise(
////            @ModelAttribute AddMerchandiseDto dto,
////            @RequestPart("image") MultipartFile imageFile
////    ) throws Exception {
////        ApiResponseDto<MerchandiseResponseDto> response = merchandiseService.addMerchandise(dto, imageFile);
////        return ResponseEntity.ok(response);
////    }
////
////
////    @GetMapping("/all/merchandise")
////    @PreAuthorize("hasAnyAuthority('OWNER', 'CENTRAL_OFFICE','DISTRIBUTOR')")
////    public ResponseEntity<ApiResponseDto<Page<MerchandiseResponseDto>>> getAllMerchandise(
////            @RequestParam(defaultValue = "0") int page,
////            @RequestParam(defaultValue = "10") int size,
////            @RequestParam(required = false) String search,
////            @RequestParam(required = false) String sort // rewardPointsAsc / rewardPointsDesc
////    ) {
////        ApiResponseDto<Page<MerchandiseResponseDto>> response =
////                merchandiseService.getAllMerchandise(page, size, search, sort);
////        return ResponseEntity.ok(response);
////    }
////
////    @PutMapping(value = "/update/merchandise/{id}", consumes = {"multipart/form-data"})
////    @PreAuthorize("hasAnyAuthority('OWNER', 'CENTRAL_OFFICE')")
////    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> updateMerchandise(
////            @PathVariable Long id,
////            @ModelAttribute AddMerchandiseDto dto,
////            @RequestPart(value = "image", required = false) MultipartFile imageFile
////    ) throws Exception {
////        ApiResponseDto<MerchandiseResponseDto> response = merchandiseService.updateMerchandise(id, dto, imageFile);
////        return ResponseEntity.ok(response);
////    }
////
////
////    @DeleteMapping("/delete/merchandise/{id}")
////    @PreAuthorize("hasAnyAuthority('OWNER', 'CENTRAL_OFFICE')")
////    public ResponseEntity<ApiResponse<Void>> deleteMerchandise(@PathVariable Long id) {
////        ApiResponse<Void> response = merchandiseService.softDeleteMerchandise(id);
////        return ResponseEntity.ok(response);
////    }
//}
