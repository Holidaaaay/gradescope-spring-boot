package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.controller.AuthController;
import com.example.gradescopespringboot.dto.auth.RegisterRequestDTO;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.service.AuthService;
import com.example.gradescopespringboot.service.UserService;
import com.example.gradescopespringboot.vo.auth.RegisterResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    @Autowired
    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterResponseVO register(RegisterRequestDTO registerRequestDTO) {
        String username = registerRequestDTO.getUsername();
        String password = registerRequestDTO.getPassword();

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username cannot be empty");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        User existingUser = userService.getByUsername(username);
        if (existingUser != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRealName(username.trim());
        user.setEmail(username.trim() + "@temp.com");
        user.setStatus(1);
        user.setIsDeleted(0);

        userService.save(user);

        return new RegisterResponseVO(user.getId(), user.getUsername());
    }
}
