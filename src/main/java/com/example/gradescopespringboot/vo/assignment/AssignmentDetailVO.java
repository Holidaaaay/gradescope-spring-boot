package com.example.gradescopespringboot.vo.assignment;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssignmentDetailVO {

    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private BigDecimal totalScore;
    private LocalDateTime dueTime;
    private Boolean allowLateSubmission;
    private Integer maxSubmissionTimes;
    private Integer status;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AssignmentFileVO> files;
}
