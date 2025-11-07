package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.CreateChiefSupervisorRequestDto;
import com.inventory.inventorymanagementsystem.dto.ChiefSupervisorResponseDto;
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

    /**
     * Create a new Chief Supervisor and map to a factory
     */
    @Transactional
    public ApiResponseDto<ChiefSupervisorResponseDto> createChiefSupervisor(CreateChiefSupervisorRequestDto dto) {
        // Step 1: Validate factory
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + dto.getFactoryId()));

        // Step 2: Check if user already exists
        User supervisor = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElse(null);

        // Step 3: Get CHIEF_SUPERVISOR role
        Role supervisorRole = roleRepository.findByRoleName(RoleName.CHIEFSUPERVISOR)
                .orElseThrow(() -> new RuntimeException("CHIEF_SUPERVISOR role not found"));

        if (supervisor == null) {
            supervisor = new User();
            supervisor.setEmail(dto.getEmail());
            supervisor.setUsername(dto.getName());
            supervisor.setPassword(passwordEncoder.encode("default123"));
            supervisor.setRole(supervisorRole);
            supervisor.setIsActive(ActiveStatus.YES);
            userRepository.save(supervisor);
        } else if (!supervisor.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
            return new ApiResponseDto<>(false, "User exists but is not a Chief Supervisor", null);
        }

        // Step 4: Map supervisor to factory
        UserFactoryMapping mapping = new UserFactoryMapping();
        mapping.setUser(supervisor);
        mapping.setFactory(factory);
        mapping.setAssignedRole(RoleName.CHIEFSUPERVISOR);
        userFactoryRepository.save(mapping);

        ChiefSupervisorResponseDto response = new ChiefSupervisorResponseDto(
                supervisor.getId(),
                supervisor.getUsername(),
                supervisor.getEmail(),
                supervisor.getPhoneNumber(),
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
                    u.getPhoneNumber(),
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
}
