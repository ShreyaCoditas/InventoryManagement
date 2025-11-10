package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.paginationsortingdto.WorkerFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.specifications.WorkerSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service

public class PlantHeadService {

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  FactoryRepository factoryRepository;

    @Autowired
    private  RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserFactoryMappingRepository userFactoryMappingRepository;

    @Autowired
    private BayRepository bayRepository;

    private static final int MAX_CAPACITY = 50;


//    public PlantHeadResponseDto createPlantHead(CreatePlantHeadRequestDto request, User owner) {
//        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD.name())
//                .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));
//        User existingUser = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);
//        User savedPlantHead;
//        if (existingUser == null) {
//            User user = new User();
//            user.setUsername(request.getUsername());
//            user.setEmail(request.getEmail());
//            String generatedPassword = "default123"; // You can randomize later if needed
//            user.setPassword(passwordEncoder.encode(generatedPassword));
//            user.setRole(role);
//            user.setIsActive(ActiveStatus.ACTIVE);
//            user.setCreatedAt(LocalDateTime.now());
//            savedPlantHead = userRepository.save(user);
//            try {
//                emailService.sendCredentialsEmail(
//                        savedPlantHead.getEmail(),
//                        savedPlantHead.getUsername(),
//                        generatedPassword,
//                        "PLANT HEAD"
//                );
//                log.info("Email sent to Plant Head: {}", savedPlantHead.getEmail());
//            } catch (Exception e) {
//                log.error(" Failed to send email to Plant Head: {}", savedPlantHead.getEmail(), e);
//            }
//
//        } else {
//            // If user already exists and is a PLANTHEAD, reuse them
//            if (!existingUser.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
//                throw new RuntimeException("User exists but is not a Plant Head");
//            }
//            savedPlantHead = existingUser;
//        }
//        if (request.getFactoryId() != null) {
//            Factory factory = factoryRepository.findById(request.getFactoryId())
//                    .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + request.getFactoryId()));
//
//            // Prevent overwriting if the factory already has the same Plant Head
//            if (factory.getPlantHead() != null && factory.getPlantHead().getId().equals(savedPlantHead.getId())) {
//            } else {
//                factory.setPlantHead(savedPlantHead);
//                factoryRepository.save(factory);
//            }
//        }
//        return new PlantHeadResponseDto(savedPlantHead.getId());
//    }

    @Transactional
    public PlantHeadResponseDto createPlantHead(CreatePlantHeadRequestDto request, User owner) {

        String generatedPassword = "default123";
        Role plantHeadRole = roleRepository.findByRoleName(RoleName.PLANTHEAD.name())
                .orElseThrow(() -> new IllegalStateException("PLANTHEAD role not seeded"));
        User plantHead = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(request.getUsername());
                    newUser.setEmail(request.getEmail());
                    newUser.setPassword(passwordEncoder.encode(generatedPassword));
                    newUser.setRole(plantHeadRole);  // ← CRITICAL: SET ROLE
                    newUser.setIsActive(ActiveStatus.ACTIVE);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
        if (plantHead.getRole() == null ||
                !plantHead.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
            throw new RuntimeException("User exists but is not a Plant Head");
        }
        // Assign to factory...
        if (request.getFactoryId() != null) {
            Factory factory = factoryRepository.findById(request.getFactoryId())
                    .orElseThrow(() -> new RuntimeException("Factory not found"));

            if (!Objects.equals(factory.getPlantHead(), plantHead)) {
                factory.setPlantHead(plantHead);
                factoryRepository.save(factory);
            }
        }
        // Send email only for new users
        if (plantHead.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
            try {
                emailService.sendCredentialsEmail(
                        plantHead.getEmail(),
                        plantHead.getUsername(),
                        generatedPassword,
                        "PLANT HEAD"
                );
            } catch (Exception e) {
                log.error("Failed to send email to {}", plantHead.getEmail(), e);
            }
        }
        return new PlantHeadResponseDto(plantHead.getId());
    }


    @Transactional
    public ApiResponseDto<Void> softDeletePlantHead(Long id) {
        User plantHead = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant Head not found"));
        if (plantHead.getIsActive() == ActiveStatus.INACTIVE) {
            return new ApiResponseDto<>(false, "Plant Head already inactive", null);
        }
        plantHead.setIsActive(ActiveStatus.INACTIVE);
        userRepository.save(plantHead);
        return new ApiResponseDto<>(true, "Plant Head deleted (soft) successfully", null);
    }

    public ApiResponseDto<List<PlantHeadDto>> getAllPlantHeads() {
        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD.name())
                .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));

        List<User> plantHeads = userRepository.findByRole(role);

        List<PlantHeadDto> result = plantHeads.stream()
                .map(u -> new PlantHeadDto(u.getId(), u.getUsername(), u.getEmail(), u.getIsActive().name()))
                .toList();

        return new ApiResponseDto<>(true, "Plant Heads fetched successfully", result);
    }

    @Transactional
    public ApiResponseDto<WorkerResponseDto> createWorker(CreateWorkerRequestDto dto, UserPrincipal currentUser) {
        RoleName role = currentUser.getUser().getRole().getRoleName();
        Long factoryId;
        if (role.equals(RoleName.PLANTHEAD)) {
            if (dto.getFactoryId() == null) {
                return new ApiResponseDto<>(false, "Factory ID is required for Plant Head", null);
            }
            factoryId = dto.getFactoryId();
        }
        else if (role.equals(RoleName.CHIEFSUPERVISOR)) {
            factoryId = userFactoryMappingRepository.findFactoryIdByUserId(currentUser.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Factory not found for Chief Supervisor"));
        }
        else {
            return new ApiResponseDto<>(false, "Only Plant Head or Chief Supervisor can create workers", null);
        }
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new RuntimeException("Factory not found"));
        Bay bay = bayRepository.findById(dto.getBayId())
                .orElseThrow(() -> new RuntimeException("Bay not found"));
        if (!bay.getFactory().getId().equals(factory.getId())) {
            return new ApiResponseDto<>(false, "Bay does not belong to this factory", null);
        }
        long currentWorkers = userFactoryMappingRepository.countByBayIdAndAssignedRole(bay.getId(), RoleName.WORKER);
        final int MAX_CAPACITY = 50; // fixed limit per bay
        if (currentWorkers >= MAX_CAPACITY) {
            return new ApiResponseDto<>(false, "Bay capacity full. Select another bay.", null);
        }

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            return new ApiResponseDto<>(false, "User already exists with this email", null);
        }
        Role workerRole = roleRepository.findByRoleName(RoleName.WORKER.name())
                .orElseThrow(() -> new RuntimeException("Role not found: WORKER"));
        String generatedPassword = "default123";
        User worker = new User();
        worker.setUsername(dto.getName());
        worker.setEmail(dto.getEmail());
        worker.setPassword(passwordEncoder.encode(generatedPassword));
        worker.setRole(workerRole);
        worker.setIsActive(ActiveStatus.ACTIVE);
        worker.setCreatedAt(LocalDateTime.now());
        userRepository.save(worker);

        //  Map worker → factory → bay
        UserFactoryMapping mapping = new UserFactoryMapping();
        mapping.setUser(worker);
        mapping.setFactory(factory);
        mapping.setBayId(String.valueOf(bay.getId()));
        mapping.setAssignedRole(RoleName.WORKER);
        userFactoryMappingRepository.save(mapping);

        try {
            emailService.sendCredentialsEmail(
                    worker.getEmail(),
                    worker.getUsername(),
                    generatedPassword,
                    "WORKER"
            );
        } catch (Exception e) {
            log.error(" Failed to send worker email: {}", worker.getEmail(), e);
        }

        // Build response
        WorkerResponseDto response = new WorkerResponseDto(
                worker.getId(),
                worker.getUsername(),
                worker.getEmail(),
                factory.getId(),
                factory.getName(),
                bay.getId(),
                bay.getBayName(),
                worker.getIsActive().name()
        );

        return new ApiResponseDto<>(true, "Worker created successfully and assigned to " + bay.getBayName(), response);
    }

    public ApiResponseDto<List<BayDropdownDto>> getAvailableBays(Long factoryId) {
        log.info("🔍 Fetching available bays for factoryId: {}", factoryId);
        List<Bay> bays = bayRepository.findByFactoryId(factoryId);
        if (bays.isEmpty()) {
            return new ApiResponseDto<>(false, "No bays found for this factory", null);
        }
        List<BayDropdownDto> availableBays = bays.stream()
                .filter(bay -> {
                    long workerCount = userFactoryMappingRepository
                            .countByBayIdAndAssignedRole(bay.getId(), RoleName.WORKER);
                    return workerCount < MAX_CAPACITY;
                })
                .map(bay -> new BayDropdownDto(bay.getId(), bay.getBayName()))
                .collect(Collectors.toList());
        if (availableBays.isEmpty()) {
            return new ApiResponseDto<>(false, "No available bays found (all full)", null);
        }
        return new ApiResponseDto<>(true, "Available bays fetched successfully", availableBays);
    }

    @Transactional
    public ApiResponseDto<List<WorkerListResponseDto>> getAllWorkers(WorkerFilterSortDto filter, Long factoryId) {
        List<UserFactoryMapping> mappings;

        if (factoryId != null) {
            mappings = userFactoryMappingRepository.findByFactoryIdAndAssignedRole(factoryId, RoleName.WORKER);
        } else {
            mappings = userFactoryMappingRepository.findByAssignedRole(RoleName.WORKER);
        }

        Stream<UserFactoryMapping> stream = mappings.stream();

        if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
            stream = stream.filter(m -> m.getFactory().getCity().equalsIgnoreCase(filter.getLocation()));
        }

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            stream = stream.filter(m -> m.getUser().getIsActive().name().equalsIgnoreCase(filter.getStatus()));
        }

        List<UserFactoryMapping> filteredMappings = stream.toList();
        List<WorkerListResponseDto> workers = filteredMappings.stream()
                .map(mapping -> {
                    User worker = mapping.getUser();
                    Factory factory = mapping.getFactory();

                    Bay bay = null;
                    if (mapping.getBayId() != null) {
                        bay = bayRepository.findById(Long.parseLong(mapping.getBayId())).orElse(null);
                    }

                    return WorkerListResponseDto.builder()
                            .workerId(worker.getId())
                            .workerName(worker.getUsername())
                            .factoryName(factory.getName())
                            .location(factory.getCity())
                            .bayArea(bay != null ? bay.getBayName() : "-")
                            .status(worker.getIsActive().name())
                            .build();
                })
                .toList();
        if ("desc".equalsIgnoreCase(filter.getSortDirection())) {
            workers = workers.stream()
                    .sorted(Comparator.comparing(WorkerListResponseDto::getWorkerName).reversed())
                    .toList();
        } else {
            workers = workers.stream()
                    .sorted(Comparator.comparing(WorkerListResponseDto::getWorkerName))
                    .toList();
        }

        return new ApiResponseDto<>(true, "Workers fetched successfully", workers);
    }

}
