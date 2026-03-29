package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.auth.RegisterRequestDTO;
import com.example.gradescopespringboot.service.AuthService;
import com.example.gradescopespringboot.vo.auth.RegisterResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

}
