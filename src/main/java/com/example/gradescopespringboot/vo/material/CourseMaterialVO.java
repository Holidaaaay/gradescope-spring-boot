package com.example.gradescopespringboot.vo.material;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseMaterialVO {

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
}
