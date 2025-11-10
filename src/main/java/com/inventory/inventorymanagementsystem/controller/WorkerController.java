package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.Bay;
import com.inventory.inventorymanagementsystem.paginationsortingdto.WorkerFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.BayRepository;
import com.inventory.inventorymanagementsystem.repository.UserFactoryMappingRepository;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.PlantHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    @Autowired
    private PlantHeadService plantHeadService;

    @Autowired
    private BayRepository bayRepository;

    @Autowired
    private UserFactoryMappingRepository userFactoryMappingRepository;

    @PostMapping("/create/workers")
    public ResponseEntity<ApiResponseDto<WorkerResponseDto>> createWorker(
            @RequestBody CreateWorkerRequestDto request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ApiResponseDto<WorkerResponseDto> response = plantHeadService.createWorker(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{factoryId}/available-bays")
    public ResponseEntity<ApiResponseDto<List<BayDropdownDto>>> getAvailableBays(@PathVariable Long factoryId) {
        ApiResponseDto<List<BayDropdownDto>> response = plantHeadService.getAvailableBays(factoryId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/allworkers")
    public ResponseEntity<ApiResponseDto<List<WorkerListResponseDto>>> getAllWorkers(
            @RequestParam(required = false) Long factoryId,
            @ModelAttribute WorkerFilterSortDto filter) {

        ApiResponseDto<List<WorkerListResponseDto>> response = plantHeadService.getAllWorkers(filter, factoryId);
        return ResponseEntity.ok(response);
    }

}




