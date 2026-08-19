package com.example.gradescopespringboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentFile {

    /**
     * File primary key
     */
    private Long id;

    /**
     * Assignment ID
     */
    private Long assignmentId;

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
     * Uploader user ID
     */
    private Long uploadedBy;

    /**
     * Created time
     */
    private LocalDateTime createdAt;
}
