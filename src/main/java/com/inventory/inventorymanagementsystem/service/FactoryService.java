package com.inventory.inventorymanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.paginationsortingdto.FactoryFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.specifications.FactorySpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service

public class FactoryService {

    @Autowired
    private  FactoryRepository factoryRepository;

    @Autowired
    private FactoryProductionRepository factoryProductionRepository;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private UserFactoryMappingRepository userFactoryMappingRepository;

    @Autowired
    private ToolStorageMappingRepository toolStorageMappingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public FactoryResponseDto createFactory(CreateFactoryRequestDto request, User owner) {
        Factory factory = objectMapper.convertValue(request, Factory.class);
        factory.setIsActive(ActiveStatus.ACTIVE);
        factory.setCreatedAt(LocalDateTime.now());
        if (request.getPlantHeadId() != null) {
            userRepository.findById(request.getPlantHeadId())
                    .ifPresent(factory::setPlantHead);
        }
        Factory savedFactory = factoryRepository.save(factory);
        return new FactoryResponseDto(savedFactory.getId());
    }

    @Transactional
    public FactoryResponseDto updateFactory(UpdateFactoryRequestDto request, User owner) {
        Factory factory = factoryRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + request.getId()));
        if (factory.getIsActive() == ActiveStatus.INACTIVE) {
            throw new RuntimeException("Cannot update a deleted (inactive) factory.");
        }
        Factory update = objectMapper.convertValue(request, Factory.class);
        if (update.getName() != null) factory.setName(update.getName());
        if (update.getCity() != null) factory.setCity(update.getCity());
        if (update.getAddress() != null) factory.setAddress(update.getAddress());
        if (request.getPlantHeadId() != null) {
            User newPlantHead = userRepository.findById(request.getPlantHeadId())
                    .orElseThrow(() -> new RuntimeException("Plant Head not found with ID: " + request.getPlantHeadId()));
            factory.setPlantHead(newPlantHead);
        }
        factory.setUpdatedAt(LocalDateTime.now());
        Factory updated = factoryRepository.save(factory);
        return new FactoryResponseDto(updated.getId());
    }

    @Transactional
    public ApiResponseDto<Void> softDeleteFactory(Long id) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factory not found"));
        if (factory.getIsActive() == ActiveStatus.INACTIVE) {
            return new ApiResponseDto<>(false, "Factory already inactive", null);
        }
        factory.setIsActive(ActiveStatus.INACTIVE);
        factoryRepository.save(factory);
        return new ApiResponseDto<>(true, "Factory deleted (soft) successfully", null);
    }


    public ApiResponseDto<List<PlantHeadFactoryResponseDto>> getFactoriesByPlantHeadId(Long plantHeadId) {
        List<Factory> factories = factoryRepository.findByPlantHeadId(plantHeadId);
        List<PlantHeadFactoryResponseDto> result = factories.stream()
                .map(f -> new PlantHeadFactoryResponseDto(f.getId(), f.getName(), f.getCity()))
                .toList();
        return new ApiResponseDto<>(true, "Factories fetched successfully", result);
    }


//    @Transactional
//    public ApiResponseDto<List<FactoryDto>> getAllFactories(FactoryFilterSortDto filter) {
//        Specification<Factory> spec = FactorySpecifications.withFilters(
//                filter.getLocation(),
//                filter.getPlantHeadName(),
//                filter.getStatus()
//        );
//        Sort sort = Sort.by(filter.getSortBy());
//        if ("desc".equalsIgnoreCase(filter.getSortDirection())) {
//            sort = sort.descending();
//        }
//        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
//        Page<Factory> factoryPage = factoryRepository.findAll(spec, pageable);
//        List<FactoryDto> factories = factoryPage.getContent().stream().map(factory -> {
//
//            String plantHeadName = factory.getPlantHead() != null
//                    ? factory.getPlantHead().getUsername()
//                    : "Unassigned";
//
//            // Total products (from production table)
//            int totalProducts = factoryProductionRepository.findTotalProducedQuantityByFactoryId(factory.getId());
//
//            // Total workers (from user-factory mapping)
//            int totalWorkers = userFactoryMappingRepository.countByFactoryIdAndAssignedRole(factory.getId(), RoleName.WORKER);
//
//            // Total tools (from inventory stock or tools table)
//            int totalTools = toolStorageMappingRepository.countByFactoryId(factory.getId());
//            return new FactoryDto(
//                    factory.getId(),
//                    factory.getName(),
//                    factory.getCity(),
//                    plantHeadName,
//                    chiefsupervisor,
//                    totalProducts,
//                    totalWorkers,
//                    totalTools,
//                    factory.getIsActive().name()
//            );
//        }).toList();
//        Map<String, Object> pagination = PaginationUtil.build(factoryPage);
//
//        return new ApiResponseDto<>(
//                true,
//                "Factories fetched successfully",
//                factories,
//                pagination
//        );
//    }


    @Transactional
    public ApiResponseDto<List<FactoryDto>> getAllFactories(FactoryFilterSortDto filter) {
        Specification<Factory> spec = FactorySpecifications.withFilters(filter.getLocation(), filter.getPlantHeadName(), filter.getStatus());

        Sort sort = Sort.by(filter.getSortBy());
        if ("desc".equalsIgnoreCase(filter.getSortDirection())) {
            sort = sort.descending();
        }

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Page<Factory> factoryPage = factoryRepository.findAll(spec, pageable);

        List<FactoryDto> factories = factoryPage.getContent().stream().map(factory -> {
            String plantHeadName = factory.getPlantHead() != null
                    ? factory.getPlantHead().getUsername()
                    : "Unassigned";

            // Fetch Chief Supervisor
            List<String> chiefs = userFactoryMappingRepository.findChiefSupervisorsByFactoryId(factory.getId());
            String chiefSupervisorName = chiefs.isEmpty() ? "Unassigned" : String.join(", ", chiefs);

            //  Other metrics
            int totalProducts = factoryProductionRepository.findTotalProducedQuantityByFactoryId(factory.getId());
            int totalWorkers = userFactoryMappingRepository.countByFactoryIdAndAssignedRole(factory.getId(), RoleName.WORKER);
            int totalTools = toolStorageMappingRepository.countByFactoryId(factory.getId());

            return new FactoryDto(
                    factory.getId(),
                    factory.getName(),
                    factory.getCity(),
                    plantHeadName,
                    totalProducts,
                    totalWorkers,
                    totalTools,
                    factory.getIsActive().name(),
                    chiefSupervisorName
            );
        }).toList();

        Map<String, Object> pagination = PaginationUtil.build(factoryPage);

        return new ApiResponseDto<>(
                true,
                "Factories fetched successfully",
                factories,
                pagination
        );
    }

}
