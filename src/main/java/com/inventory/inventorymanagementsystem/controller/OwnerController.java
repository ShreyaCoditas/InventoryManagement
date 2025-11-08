package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.paginationsortingdto.FactoryFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.UserFilterSortDto;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.*;
import com.inventory.inventorymanagementsystem.service.CentralOfficeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @Autowired
    private  FactoryService factoryService;

    @Autowired
    private PlantHeadService plantHeadService;

    @Autowired
    private CentralOfficeService centralOfficeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ChiefSupervisorService chiefSupervisorService;

    @Autowired
    private UserService userService;



    @PostMapping("/create-factory")
    public ResponseEntity<ApiResponseDto<FactoryResponseDto>> createFactory(
            @RequestBody CreateFactoryRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User owner = userPrincipal.getUser();
        FactoryResponseDto factoryResponse = factoryService.createFactory(request, owner);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Factory created successfully", factoryResponse));
    }

    @DeleteMapping("/factories/{id}/delete")
    public ApiResponseDto<Void> softDeleteFactory(@PathVariable Long id) {
        return factoryService.softDeleteFactory(id);
    }


    @PostMapping("/create-planthead")
    public ResponseEntity<ApiResponseDto<PlantHeadResponseDto>> createPlantHead(
            @RequestBody CreatePlantHeadRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User owner = userPrincipal.getUser();
        PlantHeadResponseDto created = plantHeadService.createPlantHead(request, owner);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "PlantHead created successfully", created));
    }

    @GetMapping("/plantheads")
    public ResponseEntity<ApiResponseDto<List<PlantHeadDto>>> getAllPlantHeads() {
        ApiResponseDto<List<PlantHeadDto>> response = plantHeadService.getAllPlantHeads();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/plantheads/{id}/delete")
    public ApiResponseDto<Void> softDeletePlantHead(@PathVariable Long id) {
        return plantHeadService.softDeletePlantHead(id);
    }

    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponseDto<List<FactoryListDto>>> getUnassignedFactories() {
        return ResponseEntity.ok(plantHeadService.getUnassignedFactories());
    }



    @PostMapping("/add-central-officer")
    public ResponseEntity<ApiResponseDto<Void>> addCentralOfficer(
            @Valid @RequestBody AddCentralOfficerDto request
    ) {
        ApiResponseDto<Void> response = centralOfficeService.addCentralOfficerToExistingOffice(request);
        return ResponseEntity.ok(response);
    }




    @PutMapping("/update/central-office")

    public ResponseEntity<ApiResponseDto<Void>> updateCentralOffice(
            @RequestBody UpdateCentralOfficeDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        ApiResponseDto<Void> response = centralOfficeService.updateCentralOffice(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/soft-delete/central-officer/{id}")
    public ResponseEntity<ApiResponseDto<Void>> softDeleteCentralOfficer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        System.out.println("Owner " + userPrincipal.getUsername() + " requested soft delete for officer ID: " + id);

        ApiResponseDto<Void> response = centralOfficeService.softDeleteCentralOfficer(id);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/create-product")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createOrUpdateProduct(
            @Valid @RequestBody CreateOrUpdateProductDto request) {
        return ResponseEntity.ok(productService.createOrUpdateProduct(request));
    }

    @GetMapping("/products")
//    @PreAuthorize("hasAnyRole('OWNER','PLANTHEAD','CENTRAL_OFFICER')")
    public ResponseEntity<ApiResponseDto<List<ProductResponseDto>>> getAllProducts(
            @ModelAttribute ProductFilterSortDto filter) {
        return ResponseEntity.ok(productService.getAllProducts(filter));
    }

    // DELETE (soft)
    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('OWNER','PLANTHEAD')")
    public ResponseEntity<ApiResponseDto<Void>> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

//    @PreAuthorize("hasRole('PLANTHEAD')")
    @PostMapping("/create/chiefsupervisor")
    public ResponseEntity<ApiResponseDto<ChiefSupervisorResponseDto>> createChiefSupervisor(
            @Valid @RequestBody CreateChiefSupervisorRequestDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userPrincipal.getUser();
        ApiResponseDto<ChiefSupervisorResponseDto> response =
                chiefSupervisorService.createChiefSupervisor(dto, currentUser);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/allsupervisor")
    public ResponseEntity<ApiResponseDto<List<ChiefSupervisorResponseDto>>> getAllSupervisors() {

        ApiResponseDto<List<ChiefSupervisorResponseDto>> response = chiefSupervisorService.getAllSupervisors();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("delete/supervisor/{supervisorId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteSupervisor(@PathVariable Long supervisorId) {

        ApiResponseDto<Void> response = chiefSupervisorService.softDeleteSupervisor(supervisorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/factories")
    public ResponseEntity<ApiResponseDto<List<FactoryDto>>> getAllFactories(
            @ModelAttribute FactoryFilterSortDto filter) {

        ApiResponseDto<List<FactoryDto>> response = factoryService.getAllFactories(filter);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/planthead/factories")
    public ResponseEntity<ApiResponseDto<List<PlantHeadFactoryResponseDto>>> getFactoriesByPlantHead(
            @RequestBody PlantHeadFactoryRequestDto request) {

        ApiResponseDto<List<PlantHeadFactoryResponseDto>> response =
                factoryService.getFactoriesByPlantHeadId(request.getPlantHeadId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/factories/{factoryId}/supervisors")
    public ResponseEntity<ApiResponseDto<List<FactorySupervisorsResponseDto>>> getSupervisorsByFactory(
            @PathVariable Long factoryId) {

        ApiResponseDto<List<FactorySupervisorsResponseDto>> response =
                chiefSupervisorService.getSupervisorsByFactory(factoryId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponseDto<List<UserListDto>>> getAllUsers(
            @RequestParam(required = false) String role,
            @ModelAttribute UserFilterSortDto filter) {

        // If no role is passed, fetch both PlantHead and CentralOfficer
        ApiResponseDto<List<UserListDto>> response = userService.getAllUsersByRole(role, filter);

        return ResponseEntity.ok(response);
    }




}
