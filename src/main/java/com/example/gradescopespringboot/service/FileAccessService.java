package com.example.gradescopespringboot.service;

import java.util.List;

public interface FileAccessService {

    /**
     * Resolve the course that owns a given stored file URL.
     *
     * @param fileUrl URL path, e.g. /files/assignment/uuid.pdf
     * @return owning course id
     */
    Long findCourseIdByFileUrl(String fileUrl);

    /**
     * Verify that the user is allowed to access the given course.
     *
     * @param courseId course id
     * @param userId   user id
     * @param roles    user global roles
     */
    void checkCourseAccess(Long courseId, Long userId, List<String> roles);
}
