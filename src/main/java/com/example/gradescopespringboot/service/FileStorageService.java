package com.example.gradescopespringboot.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Store an uploaded file under the given sub-directory.
     *
     * @param file        multipart file
     * @param subDirectory logical sub-directory (e.g. "assignment", "submission", "material")
     * @return public URL path of the stored file, e.g. /files/assignment/uuid.pdf
     */
    String storeFile(MultipartFile file, String subDirectory);

    /**
     * Resolve a stored file URL to a path on the local file system.
     *
     * @param fileUrl URL path returned by storeFile
     * @return resolved path
     */
    Path resolve(String fileUrl);

    /**
     * Load a stored file as a Spring Resource.
     *
     * @param fileUrl URL path returned by storeFile
     * @return resource
     */
    Resource loadAsResource(String fileUrl);
}
