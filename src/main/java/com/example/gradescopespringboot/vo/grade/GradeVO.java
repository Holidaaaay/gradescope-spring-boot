package com.example.gradescopespringboot.vo.grade;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GradeVO {

    private Long id;

    private Long submissionId;

    private Long scorerId;

    private BigDecimal score;

    private String comment;

    private LocalDateTime gradedAt;

    /**
     * Status: 0 draft, 1 final
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
