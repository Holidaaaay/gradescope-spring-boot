package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRole {

    /**
     * Primary key
     */
    private Long id;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Role ID
     */
    private Long roleId;

    /**
     * Created time
     */
    private LocalDateTime createdAt;
}
