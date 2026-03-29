package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.auth.RegisterRequestDTO;
import com.example.gradescopespringboot.vo.auth.RegisterResponseVO;

public interface AuthService {
    RegisterResponseVO register(RegisterRequestDTO registerRequestDTO);
}
