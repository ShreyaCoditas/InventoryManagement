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



    @Transactional
    public ApiResponseDto<Void> addCentralOfficerToExistingOffice(AddCentralOfficerDto dto) {

        CentralOffice office = centralOfficeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Central Office not found — please seed one before adding officers."));

        User existingUser = userRepository.findByEmailIgnoreCase(dto.getEmail()).orElse(null);
        if (existingUser != null) {
            if (existingUser.getRole().getRoleName().equals(RoleName.CENTRALOFFICER.name())) {
                return new ApiResponseDto<>(false, "User is already a Central Officer", null);
            }
            return new ApiResponseDto<>(false, "User exists but has another role", null);
        }
        Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER.name())
                .orElseThrow(() -> new RuntimeException("CENTRAL_OFFICER role not found in system"));
        User officer = new User();
        officer.setUsername(dto.getName());
        officer.setEmail(dto.getEmail());
//        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        String generatedPassword = "default123";
        officer.setPassword(passwordEncoder.encode(generatedPassword));
        officer.setRole(centralRole);
        officer.setIsActive(ActiveStatus.ACTIVE);
        userRepository.save(officer);
        UserCentralOfficeMapping mapping = new UserCentralOfficeMapping();
        mapping.setCentralOfficer(officer);
        mapping.setCentralOffice(office);
        mappingRepository.save(mapping);
        return new ApiResponseDto<>(true, "Central Officer added successfully", null);
    }


    @Transactional
    public ApiResponseDto<Void> updateCentralOffice(UpdateCentralOfficeDto dto) {
        CentralOffice office = centralOfficeRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Central Office not found with ID: " + dto.getId()));

        if (dto.getLocation() != null && !dto.getLocation().isBlank()) {
            office.setLocation(dto.getLocation());
        }
        if (dto.getCentralOfficeHeadEmail() != null && !dto.getCentralOfficeHeadEmail().isBlank()) {

            User officer = userRepository.findByEmailIgnoreCase(dto.getCentralOfficeHeadEmail()).orElse(null);

            Role centralRole = roleRepository.findByRoleName(RoleName.CENTRALOFFICER.name())
                    .orElseThrow(() -> new RuntimeException("CENTRAL_OFFICER role not found"));

            if (officer == null) {
                officer = new User();
                officer.setEmail(dto.getCentralOfficeHeadEmail());
                officer.setUsername(dto.getCentralOfficeHeadName() != null ?
                        dto.getCentralOfficeHeadName() : dto.getCentralOfficeHeadEmail());
                officer.setPassword(passwordEncoder.encode("default123"));
                officer.setRole(centralRole);
                userRepository.save(officer);
            }

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
            if (!alreadyMapped) {
                UserCentralOfficeMapping mapping = new UserCentralOfficeMapping();
                mapping.setCentralOfficer(officer);
                mapping.setCentralOffice(office);
                mappingRepository.save(mapping);
            }
        }
        centralOfficeRepository.save(office);
        return new ApiResponseDto<>(true, "Central Office updated successfully", null);
    }

    @Transactional
    public ApiResponseDto<Void> softDeleteCentralOfficer(Long officerId) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Central Officer not found with ID: " + officerId));
        if (!officer.getRole().getRoleName().equals(RoleName.CENTRALOFFICER)) {
            return new ApiResponseDto<>(false, "User is not a Central Officer", null);
        }
        officer.setIsActive(ActiveStatus.INACTIVE);
        userRepository.save(officer);
        return new ApiResponseDto<>(true, "Central Officer soft deleted successfully", null);
    }
}
