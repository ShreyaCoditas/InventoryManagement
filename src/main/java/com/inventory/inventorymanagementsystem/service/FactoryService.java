package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.CreateFactoryRequestDto;
import com.inventory.inventorymanagementsystem.dto.FactoryResponseDto;
import com.inventory.inventorymanagementsystem.dto.UpdateFactoryRequestDto;
import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.repository.FactoryRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service

public class FactoryService {

    @Autowired
    private  FactoryRepository factoryRepository;

    @Autowired
    private  UserRepository userRepository;

    @Transactional
    public FactoryResponseDto createFactory(CreateFactoryRequestDto request, User owner) {

        Factory factory = new Factory();
        factory.setName(request.getName());
        factory.setCity(request.getCity());
        factory.setAddress(request.getAddress());
        factory.setIsActive(ActiveStatus.YES);
        factory.setCreatedAt(LocalDateTime.now());

        // Optionally assign planthead if provided
        if (request.getPlantHeadId()!= null) {
            Optional<User> planthead = userRepository.findById(request.getPlantHeadId());
            planthead.ifPresent(factory::setPlantHead);
        }

        Factory savedFactory = factoryRepository.save(factory);

        return new FactoryResponseDto(savedFactory.getId());
    }


    @Transactional
    public FactoryResponseDto updateFactory(UpdateFactoryRequestDto request, User owner) {

        // Step 1: Fetch existing factory
        Factory factory = factoryRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Factory not found with ID: " + request.getId()));

        // Step 2: Update basic fields
        if (request.getName() != null) {
            factory.setName(request.getName());
        }
        if (request.getCity() != null) {
            factory.setCity(request.getCity());
        }
        if (request.getAddress() != null) {
            factory.setAddress(request.getAddress());
        }

        // Step 3: Optionally update PlantHead
        if (request.getPlantHeadId() != null) {
            User newPlantHead = userRepository.findById(request.getPlantHeadId())
                    .orElseThrow(() -> new RuntimeException("Plant Head not found with ID: " + request.getPlantHeadId()));

            factory.setPlantHead(newPlantHead);
        }

        factory.setUpdatedAt(LocalDateTime.now());
        Factory updated = factoryRepository.save(factory);

        return new FactoryResponseDto(updated.getId());
    }

    @Transactional
    public ApiResponseDto<Void> softDeleteFactory(Long id) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factory not found"));

        if (factory.getIsActive() == ActiveStatus.NO) {
            return new ApiResponseDto<>(false, "Factory already inactive", null);
        }

        factory.setIsActive(ActiveStatus.NO);
        factoryRepository.save(factory);

        return new ApiResponseDto<>(true, "Factory deleted (soft) successfully", null);
    }


//    public List<factorySummaryDto> getAllFactories() {
//        return factoryRepository.findAll() != null ? factoryRepository.findAll()
//                .stream()
//                .map(newFactory -> new factorySummaryDto(
//                        newFactory.getFactoryId(),
//                        newFactory.getName(),
//                        newFactory.getCity(),
//                        newFactory.getAddress(),
//                        newFactory.getIsActive(),
//                        newFactory.getPlantHead().getUsername()
//                ))
//                .collect(Collectors.toList()) : Collections.emptyList();
//    }
//
//    @Transactional
//    public ResponseEntity<factoryDeletionMsgDTO> deleteFactory(Long id) {
//        factory factory1 = factoryRepository.findById(id).orElseThrow(()-> new RuntimeException("Factory doesn't exists"));
//
//        // Handle related entities first to avoid constraint violations
//        // Delete user_factory relationships
////        if(userRepository.findB)
////        List<userFactory> userFactories = userFactoryRepository.findByFactory(factory1);
////        if (!userFactories.isEmpty()) {
////            userFactoryRepository.deleteAll(userFactories);
////        }
//        // Now delete the factory
//        factoryRepository.delete(factory1);
//        return ResponseEntity.ok(new factoryDeletionMsgDTO("Factory deleted successfully"));
//    }
}
