package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.auth.LoginRequestDTO;
import com.example.gradescopespringboot.dto.auth.RegisterRequestDTO;
import com.example.gradescopespringboot.vo.auth.LoginResponseVO;
import com.example.gradescopespringboot.vo.auth.RegisterResponseVO;

public interface AuthService {
    RegisterResponseVO register(RegisterRequestDTO registerRequestDTO);
    LoginResponseVO login(LoginRequestDTO loginRequestDTO);
}
