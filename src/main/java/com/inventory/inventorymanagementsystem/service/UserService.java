package com.inventory.inventorymanagementsystem.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.paginationsortingdto.UserFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.FactoryRepository;
import com.inventory.inventorymanagementsystem.repository.RoleRepository;
import com.inventory.inventorymanagementsystem.repository.UserFactoryMappingRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import com.inventory.inventorymanagementsystem.security.JWTUtil;
import com.inventory.inventorymanagementsystem.specifications.UserSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.inventory.inventorymanagementsystem.entity.Role;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserFactoryMappingRepository userFactoryMappingRepository;

    @Autowired
    private FactoryRepository factoryRepository;

    private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    public User register(RegisterDto userDto){
        String normalizedEmail=userDto.getEmail().toLowerCase();
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user=objectMapper.convertValue(userDto,User.class);
        user.setEmail(normalizedEmail);
        user.setPassword(encoder.encode(userDto.getPassword()));
        Role defaultRole = roleRepository.findByRoleName(RoleName.DISTRIBUTOR.name())
                .orElseThrow(()->new RuntimeException("Role not found"));
        user.setRole(defaultRole);
        return userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto userDto) {
        User user = userRepository.findByEmailIgnoreCase(userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDto.getEmail(), userDto.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String jwt = jwtUtil.generateToken(userDto.getEmail().toLowerCase());
                String role = user.getRole().getRoleName().name();
                return new LoginResponseDto(user.getId(), user.getUsername(), user.getEmail(), jwt, role);
            } else {
                throw new RuntimeException("Invalid password");
            }
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new RuntimeException("Invalid password");
        }
    }


@Transactional
public ApiResponseDto<List<UserListDto>> getAllUsersByRole(String roleType, UserFilterSortDto filter) {

    // BASE SPECIFICATION (name, status, date filters)
    Specification<User> spec = Specification.allOf(
            UserSpecifications.withFilters(
                    filter.getName(),
                    filter.getStatus(),
                    filter.getStatuses(),
                    filter.getCreatedAfter(),
                    filter.getCreatedBefore()
            )
    );

    // SORT + PAGE
    Sort sort = Sort.by(filter.getSortBy());
    if ("desc".equalsIgnoreCase(filter.getSortDirection())) {
        sort = sort.descending();
    }
    Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

    Page<User> userPage;
    Role role;

    switch (roleType.toUpperCase()) {

        case "PLANTHEAD" -> {
            role = roleRepository.findByRoleName(RoleName.PLANTHEAD.name())
                    .orElseThrow(() -> new RuntimeException("Role not found: PLANTHEAD"));

            // Filter users by role
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));

            userPage = userRepository.findAll(spec, pageable);
        }

        case "CHIEFSUPERVISOR" -> {
            role = roleRepository.findByRoleName(RoleName.CHIEFSUPERVISOR.name())
                    .orElseThrow(() -> new RuntimeException("Role not found: CHIEFSUPERVISOR"));

            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));

            userPage = userRepository.findAll(spec, pageable);
        }

        case "CENTRALOFFICER" -> {
            role = roleRepository.findByRoleName(RoleName.CENTRALOFFICER.name())
                    .orElseThrow(() -> new RuntimeException("Role not found: CENTRALOFFICER"));

            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));

            userPage = userRepository.findAll(spec, pageable);
        }

        default -> throw new IllegalArgumentException("Invalid role type: " + roleType);
    }

    // Convert to DTOs
    List<UserListDto> users = userPage.getContent().stream().map(u -> {

        List<String> factoryNames = null;

        // Fetch factory names based on role
        if (u.getRole().getRoleName() == RoleName.PLANTHEAD) {
            factoryNames = factoryRepository.findFactoryNamesByPlantHeadId(u.getId());
        }

        if (u.getRole().getRoleName() == RoleName.CHIEFSUPERVISOR) {
            factoryNames = userFactoryMappingRepository.findFactoryNamesByUserId(u.getId());
        }

        // Build DTO
        return new UserListDto(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole().getRoleName().name(),
                u.getIsActive(),
                u.getCreatedAt(),
                factoryNames
        );

    }).toList();

    // Pagination
    Map<String, Object> pagination = PaginationUtil.build(userPage);

    return new ApiResponseDto<>(
            true,
            "Users fetched successfully",
            users,
            pagination
    );
}





}
