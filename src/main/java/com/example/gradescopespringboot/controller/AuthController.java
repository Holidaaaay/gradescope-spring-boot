package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.auth.LoginRequestDTO;
import com.example.gradescopespringboot.dto.auth.RegisterRequestDTO;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.AuthService;
import com.example.gradescopespringboot.vo.auth.LoginResponseVO;
import com.example.gradescopespringboot.vo.auth.RegisterResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public Result<RegisterResponseVO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        RegisterResponseVO registerResponseVO =  authService.register(registerRequestDTO);
        return Result.success(registerResponseVO);
    }

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseVO loginResponseVO = authService.login(loginRequestDTO);
        return Result.success(loginResponseVO);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        Map<String, Object> data = new HashMap<>();
        data.put("userId", loginUser.getUserId());
        data.put("username", loginUser.getUsername());

        return Result.success(data);
    }
}
