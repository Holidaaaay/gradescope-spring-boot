package com.example.gradescopespringboot.vo.user;

import lombok.Data;

@Data
public class UserVO {

    /**
     * User id
     */
    private Long id;

    /**
     * Login username
     */
    private String username;

    /**
     * Real name
     */
    private String realName;

    /**
     * Email address
     */
    private String email;

    /**
     * Phone number
     */
    private String phone;

    /**
     * User number, such as student ID or employee ID
     */
    private String userNo;

    /**
     * Avatar URL
     */
    private String avatarUrl;

    /**
     * Gender: 1 male, 2 female, 0 unknown
     */
    private Integer gender;

    /**
     * User status: 1 active, 0 disabled
     */
    private Integer status;
}