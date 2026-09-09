package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.FileAccessService;
import com.example.gradescopespringboot.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileAccessService fileAccessService;

    public FileController(FileStorageService fileStorageService, FileAccessService fileAccessService) {
        this.fileStorageService = fileStorageService;
        this.fileAccessService = fileAccessService;
    }

    @GetMapping("/files/{*filePath}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filePath,
                                                 Authentication authentication) throws IOException {
        String fileUrl = "/files/" + filePath.replaceFirst("^/+", "");
        Long courseId = fileAccessService.findCourseIdByFileUrl(fileUrl);

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        fileAccessService.checkCourseAccess(courseId, loginUser.getUserId(), roles);

        Path path = fileStorageService.resolve(fileUrl);
        if (!Files.exists(path)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Stored file missing");
        }

        Resource resource = fileStorageService.loadAsResource(fileUrl);
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
