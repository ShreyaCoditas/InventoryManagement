package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.CentralOffice;
import com.inventory.inventorymanagementsystem.entity.Role;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.entity.UserCentralOfficeMapping;
import com.inventory.inventorymanagementsystem.exceptions.ResourceNotFoundException;
import com.inventory.inventorymanagementsystem.repository.CentralOfficeRepository;
import com.inventory.inventorymanagementsystem.repository.RoleRepository;
import com.inventory.inventorymanagementsystem.repository.UserCentralOfficeRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CentralOfficeService {

    private final CentralOfficeRepository centralOfficeRepository;
    private final UserRepository userRepository;
    private final UserCentralOfficeRepository mappingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponseDto<Void> addCentralOfficer(AddCentralOfficerDto dto) {
        CentralOffice office = centralOfficeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Central Office not found. Please seed one."));
        userRepository.findByEmailIgnoreCase(dto.getEmail())
                .ifPresent(u -> {
                    throw new ResourceNotFoundException(
                            u.getRole().getRoleName().equals(RoleName.CENTRALOFFICER.name())
                                    ? "User is already a Central Officer"
                                    : "User exists but has another role"
                    );
                });

        Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER.name())
                .orElseThrow(() -> new ResourceNotFoundException("CENTRAL_OFFICER role not found"));
        User officer = User.builder()
                .username(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode("default123"))
                .role(centralRole)
                .isActive(ActiveStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(officer);
        mappingRepository.save(
                UserCentralOfficeMapping.builder()
                        .centralOfficer(officer)
                        .centralOffice(office)
                        .build()
        );
        return new ApiResponseDto<>(true, "Central Officer added successfully", null);
    }


    @Transactional
    public ApiResponseDto<Void> updateCentralOfficer(Long id, AddCentralOfficerDto dto) {
        User officer = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Central Officer not found with ID: " + id));
        if (officer.getIsActive() == ActiveStatus.INACTIVE) {
            return new ApiResponseDto<>(false, "Cannot update an inactive Central Officer", null);
        }
        officer.setUsername(dto.getName());
        officer.setEmail(dto.getEmail());
        officer.setUpdatedAt(LocalDateTime.now());
        userRepository.save(officer);
        return new ApiResponseDto<>(true, "Central Officer updated successfully", null);
    }


    @Transactional
    public ApiResponseDto<Void> softDeleteCentralOfficer(Long officerId) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new IllegalStateException("Central Officer not found with ID: " + officerId));
        if (!officer.getRole().getRoleName().equals(RoleName.CENTRALOFFICER)) {
            return new ApiResponseDto<>(false, "User is not a Central Officer", null);
        }
        officer.setIsActive(ActiveStatus.INACTIVE);
        userRepository.save(officer);
        return new ApiResponseDto<>(true, "Central Officer soft deleted successfully", null);
    }
}
