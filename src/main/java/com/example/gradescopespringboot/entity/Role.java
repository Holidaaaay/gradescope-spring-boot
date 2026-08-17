package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Role {

    /**
     * Role primary key
     */
    private Long id;

    /**
     * Role code, e.g. ADMIN/STUDENT/TA/INSTRUCTOR
     */
    private String roleCode;

    /**
     * Role display name
     */
    private String roleName;

    /**
     * Role description
     */
    private String description;

    /**
     * Role status: 1 active, 0 disabled
     */
    private Integer status;

    /**
     * Created time
     */
    private LocalDateTime createdAt;

    /**
     * Updated time
     */
    private LocalDateTime updatedAt;
}
