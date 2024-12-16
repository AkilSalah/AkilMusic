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
import java.util.List;
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
                return ResponseEntity.badRequest().body("Username already exists");
            }
            System.out.println("Requested roles: " + userDTO.getRoleNames());

            List<Role> existingRoles = mongoTemplate.find(
                    Query.query(Criteria.where("name").in(userDTO.getRoleNames())),
                    Role.class,
                    "roles"
            );

            if (existingRoles.size() != userDTO.getRoleNames().size()) {
                return ResponseEntity.badRequest().body("One or more roles do not exist");
            }
            System.out.println("Existing roles: " + existingRoles);

            User user = new User();
            user.setLogin(userDTO.getLogin());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setActive(true);
            user.setRoles(new HashSet<>(existingRoles));
            user = userRepository.save(user);

            System.out.println("Saved user: " + user);

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(UserDTO.builder()
                    .id(user.getId())
                    .login(user.getLogin())
                    .active(user.isEnabled())
                    .roles(existingRoles.stream()
                            .map(role -> new RoleDTO(role.getId(), role.getName()))
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
