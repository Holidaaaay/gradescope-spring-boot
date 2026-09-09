package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.common.util.FileUtil;
import com.example.gradescopespringboot.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;
    private final long maxFileSizeBytes;

    public FileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir,
                                  @Value("${spring.servlet.multipart.max-file-size:10MB}") DataSize maxFileSize) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSize.toBytes();
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Uploaded file is empty");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Uploaded file exceeds size limit");
        }

        String originalName = FileUtil.sanitizeFileName(file.getOriginalFilename());
        if (!FileUtil.isAllowedExtension(originalName)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "File type is not allowed: " + originalName);
        }

        String extension = FileUtil.getExtension(originalName);
        String storedName = UUID.randomUUID().toString();
        if (!extension.isEmpty()) {
            storedName = storedName + "." + extension;
        }

        Path targetDirectory = rootLocation.resolve(subDirectory).normalize();
        if (!targetDirectory.startsWith(rootLocation)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid storage directory");
        }

        try {
            Files.createDirectories(targetDirectory);
            Path targetPath = targetDirectory.resolve(storedName).normalize();
            if (!targetPath.startsWith(targetDirectory)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid file name");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "Failed to store file");
        }

        return "/files/" + subDirectory + "/" + storedName;
    }

    @Override
    public Path resolve(String fileUrl) {
        String relative = stripPrefix(fileUrl);
        Path path = rootLocation.resolve(relative).normalize();
        if (!path.startsWith(rootLocation)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid file path");
        }
        return path;
    }

    @Override
    public Resource loadAsResource(String fileUrl) {
        Path path = resolve(fileUrl);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Stored file content not found");
        }
        return new FileSystemResource(path);
    }

    private String stripPrefix(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/files/")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Invalid file URL");
        }
        return fileUrl.substring("/files/".length());
    }
}
