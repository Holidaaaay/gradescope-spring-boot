package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseMember {

    /**
     * Primary key
     */
    private Long id;

    /**
     * Course ID
     */
    private Long courseId;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Role inside the course: STUDENT / TA / INSTRUCTOR
     */
    private String courseRole;

    /**
     * Join time
     */
    private LocalDateTime joinedAt;

    /**
     * Member status: 1 active, 0 removed
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
