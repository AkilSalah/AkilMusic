package com.music.controller;

import com.music.dto.RoleDTO;
import com.music.dto.UserDTO;
import com.music.exception.ResourceAlreadyExistsException;
import com.music.exception.ResourceNotFoundException;
import com.music.repository.UserRepository;
import com.music.model.Role;
import com.music.model.User;
import com.music.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MongoTemplate mongoTemplate;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            if (userRepository.existsByLogin(userDTO.getLogin())) {
                throw new ResourceAlreadyExistsException("Username already exists");
            }

            // First, find or create the USER role
            Role userRole = mongoTemplate.findOne(
                Query.query(Criteria.where("name").is("USER")),
                Role.class,
                "roles"
            );

            if (userRole == null) {
                userRole = new Role();
                userRole.setName("USER");
                userRole = mongoTemplate.save(userRole, "roles");
            }

            // Create new user
            User user = new User();
            user.setLogin(userDTO.getLogin());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setActive(true);

            // Set roles
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            // Save user
            user = userRepository.save(user);

            // Generate token
            String token = jwtService.generateToken(user);
            
            // Return complete user information
            return ResponseEntity.ok(UserDTO.builder()
                    .id(user.getId())
                    .login(user.getLogin())
                    .active(user.isEnabled())
                    .roles(user.getRoles().stream()
                        .map(role -> RoleDTO.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .build())
                        .collect(Collectors.toSet()))
                    .token(token)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .internalServerError()
                .body("Error during registration: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getLogin(), userDTO.getPassword())
            );

            User user = userRepository.findByLogin(userDTO.getLogin())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(UserDTO.builder()
                    .id(user.getId())
                    .login(user.getLogin())
                    .active(user.isEnabled())
                    .roles(user.getRoles().stream()
                        .map(role -> RoleDTO.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .build())
                        .collect(Collectors.toSet()))
                    .token(token)
                    .build());
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            throw new RuntimeException("Error during login: " + e.getMessage());
        }
    }
}
