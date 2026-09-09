package com.example.gradescopespringboot.vo.submission;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionVO {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private Integer submissionNo;
    private String contentText;
    private LocalDateTime submittedAt;
    private Boolean isLate;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
