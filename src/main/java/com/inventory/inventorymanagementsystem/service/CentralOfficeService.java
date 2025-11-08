package com.inventory.inventorymanagementsystem.service;


import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.CentralOffice;
import com.inventory.inventorymanagementsystem.entity.Role;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.entity.UserCentralOfficeMapping;
import com.inventory.inventorymanagementsystem.repository.CentralOfficeRepository;
import com.inventory.inventorymanagementsystem.repository.RoleRepository;
import com.inventory.inventorymanagementsystem.repository.UserCentralOfficeRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CentralOfficeService {

    private final CentralOfficeRepository centralOfficeRepository;
    private final UserRepository userRepository;
    private final UserCentralOfficeRepository mappingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public ApiResponseDto<Void> addCentralOfficerToExistingOffice(AddCentralOfficerDto dto) {

        // Step 1: Fetch existing central office
        CentralOffice office = centralOfficeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Central Office not found — please seed one before adding officers."));

        // Step 2: Check if officer already exists by email
        User existingUser = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElse(null);
        if (existingUser != null) {
            // Already a central officer
            if (existingUser.getRole().getRoleName().equals(RoleName.CENTRALOFFICER)) {
                return new ApiResponseDto<>(false, "User is already a Central Officer", null);
            }
            // Exists with another role
            return new ApiResponseDto<>(false, "User exists but has another role", null);
        }

        //  Step 3: Fetch CENTRAL_OFFICER role
        Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER)
                .orElseThrow(() -> new RuntimeException("CENTRAL_OFFICER role not found in system"));

        //  Step 4: Create new Central Officer
        User officer = new User();
        officer.setUsername(dto.getName());
        officer.setEmail(dto.getEmail());

        // Generate backend password
//        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
          String generatedPassword = "default123";

        officer.setPassword(passwordEncoder.encode(generatedPassword));

        officer.setRole(centralRole);
        officer.setIsActive(ActiveStatus.YES);
        userRepository.save(officer);

        //  Step 5: Map officer to existing central office
        UserCentralOfficeMapping mapping = new UserCentralOfficeMapping();
        mapping.setCentralOfficer(officer);
        mapping.setCentralOffice(office);
        mappingRepository.save(mapping);

        // Step 6: Return success response
        return new ApiResponseDto<>(true,
                "Central Officer added successfully",
                null);
    }


    @Transactional
    public ApiResponseDto<Void> updateCentralOffice(UpdateCentralOfficeDto dto) {

        // Step 1: Find existing central office
        CentralOffice office = centralOfficeRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Central Office not found with ID: " + dto.getId()));

        // Step 2: Update location if provided
        if (dto.getLocation() != null && !dto.getLocation().isBlank()) {
            office.setLocation(dto.getLocation());
        }

        // Step 3: Update head officer (optional)
        if (dto.getCentralOfficeHeadEmail() != null && !dto.getCentralOfficeHeadEmail().isBlank()) {

            // Check if officer already exists
            User officer = userRepository.findByEmailIgnoreCase(dto.getCentralOfficeHeadEmail()).orElse(null);

            // Get Central Officer role
            Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER)
                    .orElseThrow(() -> new RuntimeException("CENTRAL_OFFICER role not found"));

            // If officer doesn't exist → create one
            if (officer == null) {
                officer = new User();
                officer.setEmail(dto.getCentralOfficeHeadEmail());
                officer.setUsername(dto.getCentralOfficeHeadName() != null ?
                        dto.getCentralOfficeHeadName() : dto.getCentralOfficeHeadEmail());
                officer.setPassword(passwordEncoder.encode("default123"));
                officer.setRole(centralRole);
                userRepository.save(officer);
            }

            // Check if already mapped manually (no lambda)
            boolean alreadyMapped = false;
            List<UserCentralOfficeMapping> mappings = office.getUserCentralOfficeMappings();
            if (mappings != null) {
                for (UserCentralOfficeMapping map : mappings) {
                    if (map.getCentralOfficer().getEmail().equalsIgnoreCase(officer.getEmail())) {
                        alreadyMapped = true;
                        break;
                    }
                }
            }

            // Add mapping if not already present
            if (!alreadyMapped) {
                UserCentralOfficeMapping mapping = new UserCentralOfficeMapping();
                mapping.setCentralOfficer(officer);
                mapping.setCentralOffice(office);
                mappingRepository.save(mapping);
            }
        }

        // Step 4: Save updated office
        centralOfficeRepository.save(office);

        return new ApiResponseDto<>(true, "Central Office updated successfully", null);
    }


    @Transactional
    public ApiResponseDto<Void> softDeleteCentralOfficer(Long officerId) {
        // Step 1: Find the officer
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Central Officer not found with ID: " + officerId));

        // Step 2: Check if officer is a central officer
        if (!officer.getRole().getRoleName().equals(RoleName.CENTRALOFFICER)) {
            return new ApiResponseDto<>(false, "User is not a Central Officer", null);
        }

        // Step 3: Soft delete — mark as inactive
        officer.setIsActive(ActiveStatus.NO);
        userRepository.save(officer);

        return new ApiResponseDto<>(true, "Central Officer soft deleted successfully", null);
    }



}
