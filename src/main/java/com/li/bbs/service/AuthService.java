package com.li.bbs.service;

import com.li.bbs.domain.Role;
import com.li.bbs.domain.User;
import com.li.bbs.dto.auth.LoginRequest;
import com.li.bbs.dto.auth.LoginResponse;
import com.li.bbs.dto.auth.RegisterRequest;
import com.li.bbs.repository.RoleRepository;
import com.li.bbs.repository.UserRepository;
import com.li.bbs.security.JwtProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email exists");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setDisplayName(req.getUsername());

        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Default user role");
            roleRepository.insert(userRole);
        }

        userRepository.insert(user);
        userRepository.insertUserRole(user.getId(), userRole.getId());
    }

    public LoginResponse login(LoginRequest req) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User user = userRepository.findByUsername(req.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        List<String> roles = userRepository.findRolesByUserId(user.getId()).stream().map(Role::getName).collect(Collectors.toList());
        String token = jwtProvider.generateToken(user.getUsername(), roles);

        return new LoginResponse(token, 3600L);
    }

}

