package com.example.gradescopespringboot.vo.assignment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentFileVO {

    private Long id;
    private Long assignmentId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}
