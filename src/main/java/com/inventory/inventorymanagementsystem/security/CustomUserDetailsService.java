package com.inventory.inventorymanagementsystem.security;
import com.inventory.inventorymanagementsystem.entity.User;

import com.inventory.inventorymanagementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user=userRepository.findByEmailIgnoreCase(email.toLowerCase())
                .orElseThrow(()-> new UsernameNotFoundException("User with email " + email + " not found"));

        return new UserPrincipal(user);
    }
}
