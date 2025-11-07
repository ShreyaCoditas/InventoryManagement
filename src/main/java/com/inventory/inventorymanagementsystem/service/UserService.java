package com.inventory.inventorymanagementsystem.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.LoginRequestDto;
import com.inventory.inventorymanagementsystem.dto.LoginResponseDto;
import com.inventory.inventorymanagementsystem.dto.RegisterDto;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.repository.RoleRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import com.inventory.inventorymanagementsystem.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.inventory.inventorymanagementsystem.entity.Role;


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
        Role defaultRole = roleRepository.findByRoleName(RoleName.DISTRIBUTOR)
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
}
