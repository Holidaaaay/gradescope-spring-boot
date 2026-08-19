package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Assignment {

    /**
     * Assignment primary key
     */
    private Long id;

    /**
     * Course ID
     */
    private Long courseId;

    /**
     * Assignment title
     */
    private String title;

    /**
     * Assignment description
     */
    private String description;

    /**
     * Total score, e.g. 100.00
     */
    private BigDecimal totalScore;

    /**
     * Due time (stored as UTC / local consistent time)
     */
    private LocalDateTime dueTime;

    /**
     * Whether late submission is allowed: 0 no, 1 yes
     */
    private Integer allowLateSubmission;

    /**
     * Maximum submission times
     */
    private Integer maxSubmissionTimes;

    /**
     * Status: 0 draft, 1 published, 2 closed
     */
    private Integer status;

    /**
     * Creator user ID
     */
    private Long createdBy;

    /**
     * Updater user ID
     */
    private Long updatedBy;

    /**
     * Created time
     */
    private LocalDateTime createdAt;

    /**
     * Updated time
     */
    private LocalDateTime updatedAt;

    /**
     * Logical delete flag: 0 not deleted, 1 deleted
     */
    private Integer isDeleted;
}
