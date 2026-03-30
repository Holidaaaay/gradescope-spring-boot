package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.controller.AuthController;
import com.example.gradescopespringboot.dto.auth.LoginRequestDTO;
import com.example.gradescopespringboot.dto.auth.RegisterRequestDTO;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.security.util.JwtTokenProvider;
import com.example.gradescopespringboot.service.AuthService;
import com.example.gradescopespringboot.service.UserService;
import com.example.gradescopespringboot.vo.auth.LoginResponseVO;
import com.example.gradescopespringboot.vo.auth.RegisterResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
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

    @Override
    public LoginResponseVO login(LoginRequestDTO loginRequestDTO) {
        String username = loginRequestDTO.getUsername();
        String password = loginRequestDTO.getPassword();

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty");
        }

        User user = userService.getByUsername(username.trim());
        if (user == null) {
            throw new RuntimeException("Username or password is incorrect");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new RuntimeException("User is disabled");
        }

        if (Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new RuntimeException("User does not exist");
        }

        boolean matched = passwordEncoder.matches(password, user.getPasswordHash());
        if (!matched) {
            throw new RuntimeException("Username or password is incorrect");
        }



        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        return new LoginResponseVO(token, "Bearer");
    }
}
