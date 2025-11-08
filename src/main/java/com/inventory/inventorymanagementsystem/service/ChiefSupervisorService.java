package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.CreateChiefSupervisorRequestDto;
import com.inventory.inventorymanagementsystem.dto.ChiefSupervisorResponseDto;
import com.inventory.inventorymanagementsystem.dto.FactorySupervisorsResponseDto;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChiefSupervisorService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FactoryRepository factoryRepository;
    private final UserFactoryMappingRepository userFactoryRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public ApiResponseDto<ChiefSupervisorResponseDto> createChiefSupervisor(CreateChiefSupervisorRequestDto dto, User currentUser) {

        // Step 1: Authorization check
        if (!currentUser.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
            return new ApiResponseDto<>(false, "Only Plant Head can create a Chief Supervisor", null);
        }

        //  Step 2: Validate factory existence
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + dto.getFactoryId()));

        //  Step 3: Check if this factory already has a Chief Supervisor
        boolean supervisorExists = userFactoryRepository.existsByFactory_IdAndAssignedRole(
                dto.getFactoryId(), RoleName.CHIEFSUPERVISOR
        );
        if (supervisorExists) {
            return new ApiResponseDto<>(false, "This factory already has a Chief Supervisor assigned", null);
        }

        // Step 4: Check if user already exists
        User existingUser = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElse(null);
        if (existingUser != null && !existingUser.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
            return new ApiResponseDto<>(false, "User already exists but is not a Chief Supervisor", null);
        }

        //  Step 5: Get Chief Supervisor Role
        Role supervisorRole = roleRepository.findByRoleName(RoleName.CHIEFSUPERVISOR)
                .orElseThrow(() -> new RuntimeException("CHIEF_SUPERVISOR role not found"));

        //  Step 6: Create or reuse the user
        User supervisor = existingUser != null ? existingUser : new User();
        supervisor.setEmail(dto.getEmail());
        supervisor.setUsername(dto.getName());

        supervisor.setPassword(passwordEncoder.encode("default123"));
        supervisor.setRole(supervisorRole);
        supervisor.setIsActive(ActiveStatus.YES);
        userRepository.save(supervisor);

        // Step 7: Map supervisor to factory
        UserFactoryMapping mapping = new UserFactoryMapping();
        mapping.setUser(supervisor);
        mapping.setFactory(factory);
        mapping.setAssignedRole(RoleName.CHIEFSUPERVISOR);
        userFactoryRepository.save(mapping);

        //  Step 8: Return response
        ChiefSupervisorResponseDto response = new ChiefSupervisorResponseDto(
                supervisor.getId(),
                supervisor.getUsername(),
                supervisor.getEmail(),

                factory.getName(),
                supervisor.getIsActive().name()
        );

        return new ApiResponseDto<>(true, "Chief Supervisor created successfully", response);
    }


    /**
     *  Get all supervisors with factory details
     */
    public ApiResponseDto<List<ChiefSupervisorResponseDto>> getAllSupervisors() {
        List<UserFactoryMapping> mappings = userFactoryRepository.findByAssignedRole(RoleName.CHIEFSUPERVISOR);

        List<ChiefSupervisorResponseDto> result = mappings.stream().map(map -> {
            User u = map.getUser();
            Factory f = map.getFactory();

            return new ChiefSupervisorResponseDto(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
//                    u.getPhoneNumber(),
                    f.getName(),
                    u.getIsActive().name()
            );
        }).toList();

        return new ApiResponseDto<>(true, "Supervisors fetched successfully", result);
    }

    /**
     * Soft delete supervisor (mark inactive)
     */
    @Transactional
    public ApiResponseDto<Void> softDeleteSupervisor(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found with ID: " + supervisorId));

        if (!supervisor.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
            return new ApiResponseDto<>(false, "User is not a Chief Supervisor", null);
        }

        supervisor.setIsActive(ActiveStatus.NO);
        userRepository.save(supervisor);
        return new ApiResponseDto<>(true, "Supervisor soft deleted successfully", null);
    }

    public ApiResponseDto<List<FactorySupervisorsResponseDto>> getSupervisorsByFactory(Long factoryId) {
        // Step 1: Fetch all mappings for this factory where role = CHIEFSUPERVISOR
        List<UserFactoryMapping> mappings = userFactoryRepository
                .findByFactoryIdAndAssignedRole(factoryId, RoleName.CHIEFSUPERVISOR);

        // Step 2: Map each user to response DTO
        List<FactorySupervisorsResponseDto> supervisors = mappings.stream().map(map -> {
            User u = map.getUser();
            return new FactorySupervisorsResponseDto(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
//                    u.getPhoneNumber(),
                    u.getIsActive().name()
            );
        }).toList();

        return new ApiResponseDto<>(true, "Supervisors fetched successfully for factory ID: " + factoryId, supervisors);
    }
}
