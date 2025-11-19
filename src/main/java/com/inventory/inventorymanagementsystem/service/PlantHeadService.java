package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.exceptions.CustomException;
import com.inventory.inventorymanagementsystem.exceptions.ResourceNotFoundException;
import com.inventory.inventorymanagementsystem.paginationsortingdto.WorkerFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.specifications.WorkerSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private CloudinaryService cloudinaryService;

    private static final int MAX_CAPACITY = 50;

    @Transactional
    public PlantHeadResponseDto createPlantHead(CreatePlantHeadRequestDto request, User owner) {
//        String generatedPassword = "default123";
        String rawPassword = UUID.randomUUID().toString().substring(0, 10);  // 10-char random password

        Role plantHeadRole = roleRepository.findByRoleName(RoleName.PLANTHEAD.name())
                .orElseThrow(() -> new IllegalStateException("PLANTHEAD role not seeded"));

        // Either reuse existing user or create new Plant Head user
        User plantHead = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(request.getUsername());
                    newUser.setEmail(request.getEmail());
                    newUser.setPassword(passwordEncoder.encode(rawPassword));
                    newUser.setRole(plantHeadRole);
                    newUser.setIsActive(ActiveStatus.ACTIVE);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        if (plantHead.getRole() == null ||
                !RoleName.PLANTHEAD.equals(plantHead.getRole().getRoleName())) {
            throw new IllegalArgumentException("User exists but is not a Plant Head");
        }

        // If factoryId provided, assign Plant Head to that factory (only if factory is ACTIVE)
        if (request.getFactoryId() != null) {
            Factory factory = factoryRepository.findById(request.getFactoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

            if (factory.getIsActive() != ActiveStatus.ACTIVE) {
                throw new IllegalStateException("Cannot assign Plant Head to an inactive factory");
            }

            if (!Objects.equals(factory.getPlantHead(), plantHead)) {
                factory.setPlantHead(plantHead);
                factoryRepository.save(factory);
            }

            boolean alreadyMapped = userFactoryMappingRepository
                    .existsByUserIdAndFactoryIdAndAssignedRole(
                            plantHead.getId(), factory.getId(), RoleName.PLANTHEAD);

            if (!alreadyMapped) {
                userFactoryMappingRepository.save(UserFactoryMapping.builder()
                        .user(plantHead)
                        .factory(factory)
                        .assignedRole(RoleName.PLANTHEAD)
                        .build());
            }
        }

        // Send credentials email when user was newly created (rough heuristic: created within last minute)
        if (plantHead.getCreatedAt() != null && plantHead.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
            try {
                emailService.sendCredentialsEmail(
                        plantHead.getEmail(),
                        plantHead.getUsername(),
                        rawPassword,
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
                .orElseThrow(() -> new ResourceNotFoundException("Plant Head not found"));
        if (plantHead.getIsActive() == ActiveStatus.INACTIVE) {
            return new ApiResponseDto<>(false, "Plant Head already inactive", null);
        }
        plantHead.setIsActive(ActiveStatus.INACTIVE);
        userRepository.save(plantHead);
        return new ApiResponseDto<>(true, "Plant Head deleted (soft) successfully", null);
    }

    public ApiResponseDto<List<PlantHeadDto>> getAllPlantHeads() {
        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: PLANTHEAD"));
        List<User> plantHeads = userRepository.findByRoleAndIsActive(role, ActiveStatus.ACTIVE);
        List<PlantHeadDto> result = plantHeads.stream()
                .map(u -> new PlantHeadDto(u.getId(), u.getUsername(), u.getEmail(), u.getIsActive().name()))
                .toList();
        return new ApiResponseDto<>(true, "Plant Heads fetched successfully", result);
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


    public ApiResponseDto<List<FactoryListDto>> getUnassignedFactories() {
        List<FactoryListDto> factories = factoryRepository.findByPlantHeadIsNull().stream()
                    .map(f -> new FactoryListDto(f.getId(), f.getName()))
                    .toList();
            return new ApiResponseDto<>(true, "Unassigned factories fetched successfully", factories);
        }


    private static final String WORKER_ROLE = "WORKER";
    private static final String DEFAULT_PASSWORD = UUID.randomUUID().toString().substring(0, 10);;

    // CREATE
    public void createWorker(CreateWorkerRequestDto dto, UserPrincipal currentUser) {
        Long factoryId = resolveFactoryId(dto.getFactoryId(), currentUser);
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Bay bay = bayRepository.findById(dto.getBayId())
                .orElseThrow(() -> new ResourceNotFoundException("Bay not found"));

        validateBayInFactory(bay, factoryId);
        validateBayCapacity(bay.getId());
        validateEmailUniqueness(dto.getEmail(), null);

        Role workerRole = roleRepository.findByRoleName(RoleName.WORKER.name())
                .orElseThrow(() -> new IllegalStateException("WORKER Role not found"));

        User worker = User.builder()
                .username(dto.getName().trim())
                .email(dto.getEmail().trim())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .profileImage(cloudinaryService.uploadFile(dto.getImageFile()))
                .role(workerRole)
                .isActive(ActiveStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(worker);
        createUserFactoryMapping(worker, factory, bay);
        sendCredentialsAsync(worker);
    }


    // DELETE (soft)
    public void deleteWorker(Long id) {
        User worker = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
        worker.setIsActive(ActiveStatus.INACTIVE);
        worker.setUpdatedAt(LocalDateTime.now());
        userRepository.save(worker);
    }


    @Transactional
    public void updateWorker(Long workerId, UpdateWorkerDto dto) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            worker.setUsername(dto.getName().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            worker.setEmail(dto.getEmail().trim());
        }
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            if (worker.getProfileImage() != null) {
                cloudinaryService.delete(cloudinaryService.extractPublicId(worker.getProfileImage()));
            }
            worker.setProfileImage(cloudinaryService.uploadFile(dto.getImageFile()));
        }
        if (dto.getFactoryId() != null || dto.getBayId() != null) {
            UserFactoryMapping existingMapping = userFactoryMappingRepository.findByUserId(workerId)
                    .orElseThrow(() -> new CustomException("Worker-factory mapping not found", HttpStatus.NOT_FOUND));
            // Determine new factory (if provided)
            Factory factory = dto.getFactoryId() != null
                    ? factoryRepository.findById(dto.getFactoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Factory not found"))
                    : existingMapping.getFactory();

            // Determine new bay (if provided)
            Bay bay = dto.getBayId() != null
                    ? bayRepository.findById(dto.getBayId())
                    .orElseThrow(() -> new RuntimeException("Bay not found"))
                    : (existingMapping.getBayId() != null
                    ? bayRepository.findById(Long.parseLong(existingMapping.getBayId()))
                    .orElse(null)
                    : null);

            // Validate bay-factory relation (if bay present)
            if (bay != null && !bay.getFactory().getId().equals(factory.getId())) {
                throw new RuntimeException("Selected bay does not belong to the given factory");
            }
            existingMapping.setFactory(factory);
            existingMapping.setBayId(bay != null ? String.valueOf(bay.getId()) : null);
            userFactoryMappingRepository.save(existingMapping);
        }
        worker.setUpdatedAt(LocalDateTime.now());
        userRepository.save(worker);
    }

    @Transactional
    public ApiResponseDto<List<WorkerListResponseDto>> getAllWorkers(Long factoryId, WorkerFilterSortDto filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy().toLowerCase() : "username";
        sortBy = switch (sortBy) {
            case "workername", "username" -> "username";
            case "email", "emailid" -> "email";
            default -> sortBy;
        };
        Sort sort = "desc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        Specification<User> spec = Specification.allOf(
                WorkerSpecifications.isWorker(),
                WorkerSpecifications.search(filter.getSearch()),
                WorkerSpecifications.hasStatuses(filter.getStatus()),
                WorkerSpecifications.hasLocations(filter.getLocations()),
                WorkerSpecifications.belongsToFactory(factoryId)
        );
        Page<User> page = userRepository.findAll(spec, pageable);
        List<WorkerListResponseDto> dtos = page.getContent().stream()
                .map(this::toListDto)
                .toList();
        return new ApiResponseDto<>(true, "Workers fetched successfully", dtos, PaginationUtil.build(page));
    }




    // REUSABLE HELPERS
    private Long resolveFactoryId(Long providedId, UserPrincipal currentUser) {
        return providedId != null ? providedId :
                userFactoryMappingRepository.findFactoryIdByUserId(currentUser.getUser().getId())
                        .orElseThrow(() -> new RuntimeException("Factory not found for current user"));
    }

    private void validateBayInFactory(Bay bay, Long factoryId) {
        if (!bay.getFactory().getId().equals(factoryId)) {
            throw new RuntimeException("Bay does not belong to this factory");
        }
    }

    private void validateBayCapacity(Long bayId) {
        if (userFactoryMappingRepository.countByBayIdAndAssignedRole(bayId, RoleName.WORKER) >= 50) {
            throw new RuntimeException("Bay capacity full. Select another bay.");
        }
    }

    private void validateEmailUniqueness(String email, Long excludeId) {
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId)) {
            throw new RuntimeException("User already exists with this email");
        }
    }



    private void createUserFactoryMapping(User worker, Factory factory, Bay bay) {
        userFactoryMappingRepository.save(UserFactoryMapping.builder()
                .user(worker)
                .factory(factory)
                .bayId(String.valueOf(bay.getId()))
                .assignedRole(RoleName.WORKER)
                .build());
    }

    @Async
    public void sendCredentialsAsync(User worker) {
        emailService.sendCredentialsEmail(worker.getEmail(), worker.getUsername(), DEFAULT_PASSWORD, "WORKER");
    }

    private WorkerListResponseDto toListDto(User u) {
        UserFactoryMapping mapping = userFactoryMappingRepository.findByUserId(u.getId()).orElse(null);
        Factory factory = mapping != null ? mapping.getFactory() : null;
        Bay bay = mapping != null && mapping.getBayId() != null
                ? bayRepository.findById(Long.parseLong(mapping.getBayId())).orElse(null)
                : null;

        return WorkerListResponseDto.builder()
                .workerId(u.getId())
                .workerName(u.getUsername())
                .factoryName(factory != null ? factory.getName() : "N/A")
                .factoryId(factory != null ? factory.getId() : null)
                .location(factory != null ? factory.getCity() : "N/A")
                .bayArea(bay != null ? bay.getBayName() : "N/A")
                .status(u.getIsActive().name())
                .profileImage(u.getProfileImage())   //  Added
                .build();
    }




}
