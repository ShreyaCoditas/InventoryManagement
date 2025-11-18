package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.paginationsortingdto.FactoryFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.MerchandiseFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.UserFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.WorkerFilterSortDto;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.*;
import com.inventory.inventorymanagementsystem.service.CentralOfficeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
public class UserController {

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

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private MerchandiseService merchandiseService;

    @PostMapping("/create-factory")
    public ResponseEntity<ApiResponseDto<FactoryResponseDto>> createFactory(
            @RequestBody CreateFactoryRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User owner = userPrincipal.getUser();
        FactoryResponseDto factoryResponse = factoryService.createFactory(request, owner);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "Factory created successfully", factoryResponse));
    }

    @GetMapping("/factories")
    public ResponseEntity<ApiResponseDto<List<FactoryDto>>> getAllFactories(
            @ModelAttribute FactoryFilterSortDto filter) {

        ApiResponseDto<List<FactoryDto>> response = factoryService.getAllFactories(filter);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/factory/update")
    public ResponseEntity<ApiResponseDto<FactoryResponseDto>> updateFactory(
            @Valid @RequestBody UpdateFactoryRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FactoryResponseDto response = factoryService.updateFactory(request, currentUser.getUser());
        return ResponseEntity.ok(
                new ApiResponseDto<>(true, "Factory updated successfully", null)
        );
    }

    @DeleteMapping("/factories/{id}/delete")
    public ApiResponseDto<Void> softDeleteFactory(@PathVariable Long id) {
        return factoryService.softDeleteFactory(id);
    }

    @GetMapping("/unassigned")
    public ResponseEntity<ApiResponseDto<List<FactoryListDto>>> getUnassignedFactories() {
        return ResponseEntity.ok(plantHeadService.getUnassignedFactories());
    }

    @PostMapping("/create-planthead")
    public ResponseEntity<ApiResponseDto<PlantHeadResponseDto>> createPlantHead(
           @Valid @RequestBody CreatePlantHeadRequestDto request,
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

    @PostMapping("/add-central-officer")
    public ResponseEntity<ApiResponseDto<Void>> addCentralOfficer(
            @Valid @RequestBody AddCentralOfficerDto request
    ) {
        ApiResponseDto<Void> response = centralOfficeService.addCentralOfficer(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updateCentralOfficer(
            @PathVariable Long id,
            @Valid @RequestBody AddCentralOfficerDto dto) {

        ApiResponseDto<Void> response = centralOfficeService.updateCentralOfficer(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/soft-delete/central-officer/{id}")
    public ResponseEntity<ApiResponseDto<Void>> softDeleteCentralOfficer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        System.out.println("Owner " + userPrincipal.getUsername() + " requested soft delete for officer ID: " + id);

        ApiResponseDto<Void> response = centralOfficeService.softDeleteCentralOfficer(id);
        return ResponseEntity.ok(response);
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

//    @PostMapping("/create/workers")
//    public ResponseEntity<ApiResponseDto<WorkerResponseDto>> createWorker(
//            @RequestBody CreateWorkerRequestDto request,
//            @AuthenticationPrincipal UserPrincipal currentUser) {
//
//        ApiResponseDto<WorkerResponseDto> response = plantHeadService.createWorker(request, currentUser);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/{factoryId}/available-bays")
    public ResponseEntity<ApiResponseDto<List<BayDropdownDto>>> getAvailableBays(@PathVariable Long factoryId) {
        ApiResponseDto<List<BayDropdownDto>> response = plantHeadService.getAvailableBays(factoryId);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/allworkers")
//    public ResponseEntity<ApiResponseDto<List<WorkerListResponseDto>>> getAllWorkers(
//            @RequestParam(required = false) Long factoryId,
//            @ModelAttribute WorkerFilterSortDto filter) {
//
//        ApiResponseDto<List<WorkerListResponseDto>> response = plantHeadService.getAllWorkers(filter, factoryId);
//        return ResponseEntity.ok(response);
//    }

    // CREATE
    @PostMapping(value = "/create/worker", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<String>> create(
            @Valid @ModelAttribute CreateWorkerRequestDto dto,
            BindingResult result,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        if (result.hasErrors()) {
            String errorMsg = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, errorMsg, null));
        }

        plantHeadService.createWorker(dto, currentUser);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "worker,created", null));
    }

    // UPDATE
//    @PutMapping(value = "/worker/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponseDto<String>> update(
//            @PathVariable Long id,
//            @ModelAttribute @Valid UpdateWorkerDto dto,
//            @AuthenticationPrincipal UserPrincipal currentUser) {
//
//        plantHeadService.updateWorker(id,dto);
//        return ResponseEntity.ok(new ApiResponseDto<>(true, "worker,updated", null));
//    }

    @PutMapping("/worker/update/{id}")
    public ResponseEntity<ApiResponseDto<Void>> updateWorker(
            @PathVariable("id") Long workerId,
            @ModelAttribute UpdateWorkerDto dto) {

        try {
            // Call service to update
            plantHeadService.updateWorker(workerId, dto);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, "Worker updated successfully", null)
            );

        } catch (Exception e) {
            // Handle all exceptions gracefully
            return ResponseEntity.badRequest().body(
                    new ApiResponseDto<>(false, e.getMessage(), null)
            );
        }
    }


    // DELETE (soft)
    @DeleteMapping("/worker/delete/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable Long id) {
        plantHeadService.deleteWorker(id);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "worker,deleted", null));
    }

    // GET ALL (unchanged — already returns data)

//    @GetMapping("/worker/getall")
//    public ResponseEntity<ApiResponseDto<List<WorkerListResponseDto>>> getAll(
//            @ModelAttribute WorkerFilterSortDto filter) {
//
//        return ResponseEntity.ok(plantHeadService.getAllWorkers(filter));
//    }

//    @GetMapping("/worker/getall")
//    public ApiResponseDto<List<WorkerListResponseDto>> getAllWorkers(
//            @RequestParam Long factoryId,
//            WorkerFilterSortDto filter
//    ) {
//        return plantHeadService.getAllWorkers(factoryId, filter);
//    }

    @GetMapping("/worker/getall")
    public ApiResponseDto<List<WorkerListResponseDto>> getAllWorkers(
            @RequestParam(required = false) Long factoryId,
            WorkerFilterSortDto filter
    ) {
        return plantHeadService.getAllWorkers(factoryId, filter);
    }


    @PostMapping(value = "/add/merchandise", consumes = {"multipart/form-data"})

    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> addMerchandise(
           @Valid @ModelAttribute AddMerchandiseDto dto
    ) throws Exception {

        ApiResponseDto<MerchandiseResponseDto> response = merchandiseService.addMerchandise(dto);
        return ResponseEntity.ok(response);
    }



//    @GetMapping("/all/merchandise")
//    public ResponseEntity<ApiResponseDto<Page<MerchandiseResponseDto>>> getAllMerchandise(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) Integer minRewardPoints,
//            @RequestParam(required = false) Integer maxRewardPoints,
//            @RequestParam(required = false) String stockStatus,
//            @RequestParam(required = false) String sort
//    ) {
//        ApiResponseDto<Page<MerchandiseResponseDto>> response =
//                merchandiseService.getAllMerchandise(page, size, search, minRewardPoints, maxRewardPoints, stockStatus, sort);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/all/merchandise")
    public ResponseEntity<ApiResponseDto<List<MerchandiseResponseDto>>> getAllMerchandise(
            MerchandiseFilterSortDto filter
    ) {
        ApiResponseDto<List<MerchandiseResponseDto>> response =
                merchandiseService.getAllMerchandise(filter);

        return ResponseEntity.ok(response);
    }



    @PutMapping(value = "/update/merchandise/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponseDto<MerchandiseResponseDto>> updateMerchandise(
            @PathVariable Long id,
            @ModelAttribute UpdateMerchandiseDto dto
    ) throws Exception {

        ApiResponseDto<MerchandiseResponseDto> response = merchandiseService.updateMerchandise(id, dto);
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/delete/merchandise/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteMerchandise(@PathVariable Long id) {
        ApiResponseDto<Void> response = merchandiseService.softDeleteMerchandise(id);
        return ResponseEntity.ok(response);
    }







}
