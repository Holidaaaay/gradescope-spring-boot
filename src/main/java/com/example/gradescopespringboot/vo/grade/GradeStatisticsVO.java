package com.example.gradescopespringboot.vo.grade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeStatisticsVO {

    private Long assignmentId;

    /**
     * Number of final grades for this assignment
     */
    private Long gradedCount;

    private BigDecimal averageScore;

    private BigDecimal medianScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;
}
