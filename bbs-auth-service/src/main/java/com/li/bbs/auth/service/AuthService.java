package com.li.bbs.auth.service;

import com.li.bbs.auth.domain.Role;
import com.li.bbs.auth.domain.User;
import com.li.bbs.auth.dto.LoginRequest;
import com.li.bbs.auth.dto.LoginResponse;
import com.li.bbs.auth.dto.RegisterRequest;
import com.li.bbs.auth.repository.RoleRepository;
import com.li.bbs.auth.repository.UserRepository;
import com.li.bbs.auth.security.JwtProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setDisplayName(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        Role role = roleRepository.findByName("ROLE_USER");
        if (role == null) {
            role = new Role();
            role.setName("ROLE_USER");
            roleRepository.insert(role);
        }

        userRepository.insert(user);
        userRepository.insertUserRole(user.getId(), role.getId());
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = userRepository.findByUsername(request.username());
        if (user == null) {
            throw new IllegalArgumentException("invalid credentials");
        }

        String token = jwtProvider.generateToken(user.getId(), user.getUsername(), userRepository.findRolesByUserId(user.getId()).stream().map(Role::getName).toList());
        return new LoginResponse(token, 3600);
    }
}

