package com.example.gradescopespringboot.vo.grade;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeWithSubmissionVO {

    private Long id;

    private Long submissionId;

    private Long scorerId;

    private BigDecimal score;

    private String comment;

    private LocalDateTime gradedAt;

    /**
     * Grade status: 0 draft, 1 final
     */
    private Integer status;

    private Long studentId;

    private Integer submissionNo;

    private String contentText;

    private LocalDateTime submittedAt;

    private Boolean isLate;
}
