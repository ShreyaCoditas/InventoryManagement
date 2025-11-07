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

@Service
@RequiredArgsConstructor
public class CentralOfficeService {

    private final CentralOfficeRepository centralOfficeRepository;
    private final UserRepository userRepository;
    private final UserCentralOfficeRepository mappingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new Central Office and assigns a Central Officer (head user).
     * Only one Central Office should exist.
     */
    @Transactional
    public ApiResponseDto<Void> createCentralOffice(CreateCentralOfficeDto dto) {

        // Step 1: Ensure only one Central Office exists
        if (centralOfficeRepository.count() > 0) {
            return new ApiResponseDto<>(false, "A Central Office already exists in the system", null);
        }

        // Step 2: Validate request
        if (dto.getCentralOfficeHeadEmail() == null || dto.getCentralOfficeHeadEmail().isBlank()) {
            return new ApiResponseDto<>(false, "Central Office head email is required", null);
        }

        // Step 3: Create Central Office
        CentralOffice office = new CentralOffice();
        office.setLocation(dto.getLocation() != null ? dto.getLocation() : "Headquarters");
        centralOfficeRepository.save(office);

        // Step 4: Handle Central Officer user
        User user = userRepository.findByEmailIgnoreCase(dto.getCentralOfficeHeadEmail()).orElse(null);

        Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER)
                .orElseThrow(() -> new RuntimeException("CENTRAL_OFFICER role not found"));

        if (user == null) {
            // Create new Central Office head user
            user = new User();
            user.setEmail(dto.getCentralOfficeHeadEmail());
            user.setUsername(dto.getCentralOfficeHeadName() != null ? dto.getCentralOfficeHeadName() : dto.getCentralOfficeHeadEmail());
            user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "default123"));
            user.setRole(centralRole);
            userRepository.save(user);
        } else if (!user.getRole().getRoleName().equals(RoleName.CENTRALOFFICER)) {
            return new ApiResponseDto<>(false, "User exists but is not a Central Office user", null);
        }

        // Step 5: Map this officer to the office
        UserCentralOfficeMapping mapping = new UserCentralOfficeMapping();
        mapping.setCentralOfficer(user);
        mapping.setCentralOffice(office);
        mappingRepository.save(mapping);

        return new ApiResponseDto<>(true, "Central Office created successfully", null);
    }

    /**
     * Adds a Central Officer to the existing Central Office.
     */
    @Transactional
    public ApiResponseDto<Void> addCentralOfficerToOffice(AddCentralOfficerDto dto) {
        // Step 1: Fetch existing office (only one expected)
        CentralOffice office = centralOfficeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Central Office not found"));

        // Step 2: Check if user already exists
        User officer = userRepository.findByEmailIgnoreCase(dto.getCentralOfficerEmail()).orElse(null);

        Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER)
                .orElseThrow(() -> new RuntimeException("CENTRAL_OFFICER role not found"));

        if (officer != null) {
            if (officer.getRole().getRoleName().equals(RoleName.CENTRALOFFICER)) {
                return new ApiResponseDto<>(false, "User is already a Central Officer", null);
            }
            return new ApiResponseDto<>(false, "User exists but is not a Central Officer", null);
        }

        // Step 3: Create new Central Officer
        officer = new User();
        officer.setEmail(dto.getCentralOfficerEmail());

        officer.setUsername(dto.getCentralOfficeHeadName() != null ? dto.getCentralOfficeHeadName() : dto.getCentralOfficerEmail());

        officer.setPassword(passwordEncoder.encode("default123"));
        officer.setRole(centralRole);
        userRepository.save(officer);

        // Step 4: Map officer to the office
        UserCentralOfficeMapping mapping = new UserCentralOfficeMapping();
        mapping.setCentralOfficer(officer);
        mapping.setCentralOffice(office);
        mappingRepository.save(mapping);

        return new ApiResponseDto<>(true, "Central Officer added successfully", null);
    }

    /**
     * Fetch all Central Offices and their assigned officers.
     */
    public ApiResponseDto<List<CentralOfficeResponseDto>> getCentralOffices() {
        List<CentralOffice> offices = centralOfficeRepository.findAll();

        List<CentralOfficeResponseDto> officeDtos = offices.stream().map(office -> {
            CentralOfficeResponseDto dto = new CentralOfficeResponseDto();
            dto.setId(office.getId());
            dto.setLocation(office.getLocation());

            List<UserListDto> officers = office.getUserCentralOfficeMappings() != null
                    ? office.getUserCentralOfficeMappings().stream().map(mapping -> {
                User user = mapping.getCentralOfficer();
                UserListDto userDto = new UserListDto();
                userDto.setId(user.getId());
                userDto.setUsername(user.getUsername());
                userDto.setEmail(user.getEmail());
                userDto.setRole(user.getRole().getRoleName());
                userDto.setIsActive(user.getIsActive());
                return userDto;
            }).toList()
                    : List.of();

            dto.setOfficers(officers);
            return dto;
        }).toList();

        return new ApiResponseDto<>(true, "Central offices fetched successfully", officeDtos);
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
