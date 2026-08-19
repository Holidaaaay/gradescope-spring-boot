package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Course {

    /**
     * Course primary key
     */
    private Long id;

    /**
     * Course code, e.g. CS101
     */
    private String courseCode;

    /**
     * Course name
     */
    private String courseName;

    /**
     * Semester, e.g. 2026 Spring
     */
    private String semester;

    /**
     * Course description
     */
    private String description;

    /**
     * Classroom location
     */
    private String location;

    /**
     * Schedule information
     */
    private String scheduleInfo;

    /**
     * Course status: 1 active, 0 closed
     */
    private Integer status;

    /**
     * Creator user ID
     */
    private Long createdBy;

    /**
     * Updater user ID
     */
    private Long updatedBy;

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
