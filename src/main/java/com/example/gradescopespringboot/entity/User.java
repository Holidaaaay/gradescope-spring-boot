package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    /**
     * User primary key
     */
    private Long id;

    /**
     * Login username
     */
    private String username;

    /**
     * Encrypted password
     */
    private String passwordHash;

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

    /**
     * Last login time
     */
    private LocalDateTime lastLoginAt;

    /**
     * Created time
     */
    private LocalDateTime createdAt;

    /**
     * Updated time
     */
    private LocalDateTime updatedAt;

    /**
     * Logical delete flag: 0 not deleted, 1 deleted
     */
    private Integer isDeleted;
}