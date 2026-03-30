package com.example.gradescopespringboot.vo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO {

    private String token;

    private  String tokenType;

}
