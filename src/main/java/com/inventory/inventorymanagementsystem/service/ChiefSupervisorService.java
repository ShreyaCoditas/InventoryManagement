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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ChiefSupervisorService {
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  RoleRepository roleRepository;

    @Autowired
    private  FactoryRepository factoryRepository;

    @Autowired
    private  UserFactoryMappingRepository userFactoryRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponseDto<ChiefSupervisorResponseDto> createChiefSupervisor(CreateChiefSupervisorRequestDto dto, User currentUser) {
        if (!currentUser.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
            return new ApiResponseDto<>(false, "Only Plant Head can create a Chief Supervisor", null);
        }
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + dto.getFactoryId()));
        boolean supervisorExists = userFactoryRepository.existsByFactory_IdAndAssignedRole(
                dto.getFactoryId(), RoleName.CHIEFSUPERVISOR
        );
        if (supervisorExists) {
            return new ApiResponseDto<>(false, "This factory already has a Chief Supervisor assigned", null);
        }
        User existingUser = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElse(null);
        if (existingUser != null && !existingUser.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
            return new ApiResponseDto<>(false, "User already exists but is not a Chief Supervisor", null);
        }
        Role supervisorRole = roleRepository.findByRoleName(RoleName.CHIEFSUPERVISOR.name())
                .orElseThrow(() -> new RuntimeException("CHIEF_SUPERVISOR role not found"));
        User supervisor = existingUser != null ? existingUser : new User();
        supervisor.setEmail(dto.getEmail());
        supervisor.setUsername(dto.getName());
        supervisor.setPassword(passwordEncoder.encode("default123"));
        supervisor.setRole(supervisorRole);
        supervisor.setIsActive(ActiveStatus.ACTIVE);
        userRepository.save(supervisor);


        UserFactoryMapping mapping = new UserFactoryMapping();
        mapping.setUser(supervisor);
        mapping.setFactory(factory);
        mapping.setAssignedRole(RoleName.CHIEFSUPERVISOR);
        userFactoryRepository.save(mapping);

        ChiefSupervisorResponseDto response = new ChiefSupervisorResponseDto(
                supervisor.getId(),
                supervisor.getUsername(),
                supervisor.getEmail(),
                factory.getName(),
                supervisor.getIsActive().name()
        );
        return new ApiResponseDto<>(true, "Chief Supervisor created successfully", response);
    }


    public ApiResponseDto<List<ChiefSupervisorResponseDto>> getAllSupervisors() {
        List<UserFactoryMapping> mappings = userFactoryRepository.findByAssignedRole(RoleName.CHIEFSUPERVISOR);

        List<ChiefSupervisorResponseDto> result = mappings.stream().map(map -> {
            User u = map.getUser();
            Factory f = map.getFactory();

            return new ChiefSupervisorResponseDto(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    f.getName(),
                    u.getIsActive().name()
            );
        }).toList();
        return new ApiResponseDto<>(true, "Supervisors fetched successfully", result);
    }

    @Transactional
    public ApiResponseDto<Void> softDeleteSupervisor(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("Supervisor not found with ID: " + supervisorId));

        if (!supervisor.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
            return new ApiResponseDto<>(false, "User is not a Chief Supervisor", null);
        }
        supervisor.setIsActive(ActiveStatus.INACTIVE);
        userRepository.save(supervisor);
        return new ApiResponseDto<>(true, "Supervisor soft deleted successfully", null);
    }

    public ApiResponseDto<List<FactorySupervisorsResponseDto>> getSupervisorsByFactory(Long factoryId) {
        List<UserFactoryMapping> mappings = userFactoryRepository
                .findByFactoryIdAndAssignedRole(factoryId, RoleName.CHIEFSUPERVISOR);
        List<FactorySupervisorsResponseDto> supervisors = mappings.stream().map(map -> {
            User u = map.getUser();
            return new FactorySupervisorsResponseDto(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getIsActive().name()
            );
        }).toList();

        return new ApiResponseDto<>(true, "Supervisors fetched successfully for factory ID: " + factoryId, supervisors);
    }
}
