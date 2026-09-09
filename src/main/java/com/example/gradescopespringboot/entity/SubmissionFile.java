package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionFile {

    /**
     * File primary key
     */
    private Long id;

    /**
     * Submission ID
     */
    private Long submissionId;

    /**
     * Original file name
     */
    private String fileName;

    /**
     * File access URL / storage path
     */
    private String fileUrl;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * File type, e.g. pdf / zip
     */
    private String fileType;

    /**
     * Created time
     */
    private LocalDateTime createdAt;
}
