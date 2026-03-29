package com.example.gradescopespringboot.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {

    private String username;

    private String password;
}