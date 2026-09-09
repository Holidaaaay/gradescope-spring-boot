package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Submission {

    /**
     * Submission primary key
     */
    private Long id;

    /**
     * Assignment ID
     */
    private Long assignmentId;

    /**
     * Student user ID
     */
    private Long studentId;

    /**
     * Submission sequence number for this assignment and student
     */
    private Integer submissionNo;

    /**
     * Text content submitted by the student
     */
    private String contentText;

    /**
     * Submission time
     */
    private LocalDateTime submittedAt;

    /**
     * Whether the submission is late: 0 no, 1 yes
     */
    private Integer isLate;

    /**
     * Status: 0 draft, 1 submitted, 2 withdrawn, 3 graded
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

    /**
     * Logical delete flag: 0 not deleted, 1 deleted
     */
    private Integer isDeleted;
}
