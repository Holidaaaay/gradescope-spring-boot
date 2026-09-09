package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseMaterial {

    private Long id;

    private Long courseId;

    private String title;

    private String description;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private Long uploadedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;
}
