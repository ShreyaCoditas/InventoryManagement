package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.Role;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.repository.FactoryRepository;
import com.inventory.inventorymanagementsystem.repository.RoleRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

//    public PlantHeadResponseDto createPlantHead(CreatePlantHeadRequestDto request, User owner) {
//
//        // Get Role
//        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD)
//                .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));
//
//        // Create User
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//        user.setPassword(request.getPassword());
//        user.setRole(role);
//        user.setIsActive(ActiveStatus.YES);
//        user.setCreatedAt(LocalDateTime.now());
//
//        User savedPlantHead = userRepository.save(user);
//
//        // Assign to Factory if provided
//        if (request.getFactoryId() != null) {
//            Factory factory = factoryRepository.findById(request.getFactoryId())
//                    .orElseThrow(() -> new RuntimeException("Factory not found"));
//            factory.setPlantHead(savedPlantHead);
//            factoryRepository.save(factory);
//        }

//        return new PlantHeadResponseDto(savedPlantHead.getId());
   // }

//    public PlantHeadResponseDto createPlantHead(CreatePlantHeadRequestDto request, User owner) {
//
//        //  Step 1: Get Role
//        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD)
//                .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));
//
//        //  Step 2: Check if user already exists
//        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
//            throw new RuntimeException("User with email already exists");
//        }
//
//        // Step 3: Create new Plant Head user
//        User user = new User();
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//
//        //  Backend-generated password (can be random or default)
//        String generatedPassword = "default123"; // or use UUID.randomUUID().toString().substring(0,8);
//        user.setPassword(passwordEncoder.encode(generatedPassword));
//
//        user.setRole(role);
//        user.setIsActive(ActiveStatus.YES);
//        user.setCreatedAt(LocalDateTime.now());
//
//        User savedPlantHead = userRepository.save(user);
//
//        // Step 4: Assign to factory (optional)
//        if (request.getFactoryId() != null) {
//            Factory factory = factoryRepository.findById(request.getFactoryId())
//                    .orElseThrow(() -> new RuntimeException("Factory not found"));
//            factory.setPlantHead(savedPlantHead);
//            factoryRepository.save(factory);
//        }
//
//        //  Step 5: Return response DTO (optionally include generated password)
//        PlantHeadResponseDto response = new PlantHeadResponseDto(savedPlantHead.getId());
//        // Optional: add field for generated password if you want to send it in the response
//        // response.setGeneratedPassword(generatedPassword);
//
//        return response;
//    }

    public PlantHeadResponseDto createPlantHead(CreatePlantHeadRequestDto request, User owner) {

        // ✅ Step 1: Get Role
        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD)
                .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));

        // ✅ Step 2: Check if user already exists
        User existingUser = userRepository.findByEmailIgnoreCase(request.getEmail()).orElse(null);

        User savedPlantHead;

        if (existingUser == null) {
            // ✅ Step 3: Create new Plant Head user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());

            // 🔐 Backend-generated password
            String generatedPassword = "default123"; // You can randomize later if needed
            user.setPassword(passwordEncoder.encode(generatedPassword));

            user.setRole(role);
            user.setIsActive(ActiveStatus.YES);
            user.setCreatedAt(LocalDateTime.now());

            savedPlantHead = userRepository.save(user);
        } else {
            // ✅ If user already exists and is a PLANTHEAD, reuse them
            if (!existingUser.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
                throw new RuntimeException("User exists but is not a Plant Head");
            }
            savedPlantHead = existingUser;
        }

        // ✅ Step 4: Assign to Factory (one per request)
        if (request.getFactoryId() != null) {
            Factory factory = factoryRepository.findById(request.getFactoryId())
                    .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + request.getFactoryId()));

            // Prevent overwriting if the factory already has the same Plant Head
            if (factory.getPlantHead() != null && factory.getPlantHead().getId().equals(savedPlantHead.getId())) {
                // Do nothing, already assigned
            } else {
                factory.setPlantHead(savedPlantHead);
                factoryRepository.save(factory);
            }
        }

        // ✅ Step 5: Return response
        return new PlantHeadResponseDto(savedPlantHead.getId());
    }

    public ApiResponseDto<List<FactoryListDto>> getUnassignedFactories() {
        List<Factory> unassignedFactories = factoryRepository.findUnassignedFactories();

        // Map to DTO with plantId and plantName
        List<FactoryListDto> responseList = unassignedFactories.stream()
                .map(factory -> new FactoryListDto(
                        factory.getId(),      // maps to plantId
                        factory.getName()     // maps to plantName
                ))
                .toList();

        return new ApiResponseDto<>(
                true,
                "Unassigned factories fetched successfully",
                responseList
        );
    }


    @Transactional
    public ApiResponseDto<Void> softDeletePlantHead(Long id) {
        User plantHead = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant Head not found"));

        if (plantHead.getIsActive() == ActiveStatus.NO) {
            return new ApiResponseDto<>(false, "Plant Head already inactive", null);
        }

        plantHead.setIsActive(ActiveStatus.NO);
        userRepository.save(plantHead);

        return new ApiResponseDto<>(true, "Plant Head deleted (soft) successfully", null);
    }

    public ApiResponseDto<List<PlantHeadDto>> getAllPlantHeads() {
        Role role = roleRepository.findByRoleName(RoleName.PLANTHEAD)
                .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));

        List<User> plantHeads = userRepository.findByRole(role);

        List<PlantHeadDto> result = plantHeads.stream()
                .map(u -> new PlantHeadDto(u.getId(), u.getUsername(), u.getEmail(), u.getIsActive().name()))
                .toList();

        return new ApiResponseDto<>(true, "Plant Heads fetched successfully", result);
    }

}
