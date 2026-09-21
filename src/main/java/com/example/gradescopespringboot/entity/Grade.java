package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Grade {

    /**
     * Grade primary key
     */
    private Long id;

    /**
     * Submission ID
     */
    private Long submissionId;

    /**
     * Scorer user ID (TA / INSTRUCTOR)
     */
    private Long scorerId;

    /**
     * Score awarded
     */
    private BigDecimal score;

    /**
     * Feedback comment
     */
    private String comment;

    /**
     * Grading time
     */
    private LocalDateTime gradedAt;

    /**
     * Status: 0 draft, 1 final
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
