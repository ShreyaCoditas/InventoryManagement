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
import java.time.LocalDateTime;
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
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + dto.getFactoryId()));
        if (userFactoryRepository.existsByFactory_IdAndAssignedRole(dto.getFactoryId(), RoleName.CHIEFSUPERVISOR)) {
            return new ApiResponseDto<>(false, "Factory already has a Chief Supervisor assigned", null);
        }
        User supervisor = userRepository.findByEmailIgnoreCase(dto.getEmail())
                .map(existing -> {
                    if (!existing.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
                        throw new RuntimeException("User already exists but is not a Chief Supervisor");
                    }
                    return existing;
                })
                .orElseGet(() -> createNewChiefSupervisor(dto));
        // Create mapping (factory → supervisor)
        userFactoryRepository.save(new UserFactoryMapping(supervisor, factory, RoleName.CHIEFSUPERVISOR));
        ChiefSupervisorResponseDto response = new ChiefSupervisorResponseDto(supervisor.getId(), supervisor.getUsername(), supervisor.getEmail(), factory.getName(), supervisor.getIsActive().name());
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






    private User createNewChiefSupervisor(CreateChiefSupervisorRequestDto dto) {
        Role role = roleRepository.findByRoleName(RoleName.CHIEFSUPERVISOR.name())
                .orElseThrow(() -> new RuntimeException("Role not found: CHIEF_SUPERVISOR"));

        User user = new User();
        user.setUsername(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("default123"));
        user.setRole(role);
        user.setIsActive(ActiveStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

}
