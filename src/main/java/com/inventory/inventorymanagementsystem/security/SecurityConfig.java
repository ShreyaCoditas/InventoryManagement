package com.inventory.inventorymanagementsystem.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of(
                            "http://localhost:3000",
                            "https://intelligible-unextreme-rose.ngrok-free.dev"
                    ));
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .authorizeHttpRequests(request -> request

                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()
                                .requestMatchers("/api/owner/create-factory").hasRole("OWNER")
                                .requestMatchers("/api/owner/factories").hasRole("OWNER")
                                .requestMatchers("/api/owner/factory/update").hasRole("OWNER")
                                .requestMatchers("/api/owner/factories/{id}/delete").hasRole("OWNER")
                                .requestMatchers("/api/owner/unassigned").hasRole("OWNER")

                                .requestMatchers("/api/owner/create-planthead").hasRole("OWNER")
                                .requestMatchers("/api/owner/plantheads").hasRole("OWNER")
                                .requestMatchers("/api/owner/plantheads/{id}/delete").hasRole("OWNER")

                                .requestMatchers("/api/owner/add-central-officer").hasRole("OWNER")
                                .requestMatchers("/api/owner/update/{id}").hasRole("OWNER")
                                .requestMatchers("/api/owner/soft-delete/central-officer/{id}").hasRole("OWNER")

                                .requestMatchers("api/owner/create/chiefsupervisor").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/allsupervisor").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/delete/supervisor/{supervisorId}").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/planthead/factories").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/factories/{factoryId}/supervisors").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/users").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/{factoryId}/available-bays").hasRole("PLANTHEAD")

                                .requestMatchers("/api/owner/create/worker").hasAnyRole("PLANTHEAD","CHIEFSUPERVISOR")
                                .requestMatchers("/api/owner/worker/getall").hasAnyRole("OWNER","PLANTHEAD","CHIEFSUPERVISOR")
                                .requestMatchers("/api/owner/worker/update/{id}").hasAnyRole("PLANTHEAD","CHIEFSUPERVISOR")
                                .requestMatchers("/api/owner/worker/delete/{id}").hasAnyRole("OWNER","PLANTHEAD")
                                .requestMatchers("/api/owner/{factoryId}/available-bays").hasAnyRole("OWNER","PLANTHEAD")






                                .requestMatchers("/api/tool-request/worker").hasRole("WORKER")
                                .requestMatchers("/api/tool-request/handle/{itemId}/action").hasAnyRole("PLANTHEAD","CHIEFSUPERVISOR")
                                .requestMatchers("/api/profile/upload-image").hasRole("OWNER")
                                .requestMatchers("/api/tools/stock/add").hasRole("PLANTHEAD")
                                .requestMatchers("/api/tools/factory/{factoryId}/slots").hasRole("PLANTHEAD")
                                .requestMatchers("/api/tool-request/all/cs/requests").hasRole("CHIEFSUPERVISOR")
                                .requestMatchers("/api/tool-request/all/ph/requests").hasRole("PLANTHEAD")
                                .requestMatchers("/api/tool-request/worker/my/requests").hasRole("WORKER")
                                .requestMatchers("/api/tool-request/restock").hasRole("CHIEFSUPERVISOR")
                                .requestMatchers("/api/tool-request/ph/all/restock").hasRole("PLANTHEAD")
                                .requestMatchers("/api/tools/worker/return").hasRole("WORKER")
                                .requestMatchers("/api/tools/cs/requests").hasRole("CHIEFSUPERVISOR")
                                .requestMatchers("/api/tools/verify/return").hasRole("CHIEFSUPERVISOR")






                                .requestMatchers("/api/owner/add/merchandise").hasAnyRole("OWNER","CENTRALOFFICER")
                                .requestMatchers("/api/owner/update/merchandise/{id}").hasAnyRole("OWNER","CENTRALOFFICER")
                                .requestMatchers("/api/owner/all/merchandise").hasAnyRole("OWNER","CENTRALOFFICER")
                                .requestMatchers("/api/owner/delete/merchandise/{id}").hasAnyRole("OWNER","CENTRALOFFICER")
                                .requestMatchers("/api/tools/**").hasAnyRole("OWNER","PLANTHEAD")

                                .requestMatchers("/api/product/central-office/restock-requests").hasRole("CENTRALOFFICER")
                                .requestMatchers("/api/product/plant-head/products/add-stock").hasRole("PLANTHEAD")
                                .requestMatchers("/api/product/factories/restock-requests/{requestId}/complete").hasRole("PLANTHEAD")
                                .requestMatchers("/api/product/factories/my-restock-requests").hasRole("PLANTHEAD")
                                .requestMatchers("/api/product/add/production").hasRole("PLANTHEAD")
                                .requestMatchers("/api/product/central-office/my-restock-requests").hasRole("CENTRALOFFICER")

                                .requestMatchers("/api/product/central-office").hasRole("CENTRALOFFICER")

                                .requestMatchers("/api/products/**").hasRole("OWNER")



                        .anyRequest().authenticated()
//                                .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12);
//    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}



