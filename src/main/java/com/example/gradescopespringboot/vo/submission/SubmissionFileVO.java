package com.example.gradescopespringboot.vo.submission;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionFileVO {

    private Long id;
    private Long submissionId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private LocalDateTime createdAt;
}
