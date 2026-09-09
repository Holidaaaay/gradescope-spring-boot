package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.submission.CreateSubmissionRequestDTO;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.SubmissionService;
import com.example.gradescopespringboot.vo.submission.SubmissionDetailVO;
import com.example.gradescopespringboot.vo.submission.SubmissionFileVO;
import com.example.gradescopespringboot.vo.submission.SubmissionVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses/{courseId}/assignments/{assignmentId}/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public Result<SubmissionVO> createSubmission(@PathVariable Long courseId,
                                                   @PathVariable Long assignmentId,
                                                   @Valid @RequestBody CreateSubmissionRequestDTO dto,
                                                   Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(submissionService.createSubmission(courseId, assignmentId, dto,
                loginUser.getUserId(), roles));
    }

    @PostMapping("/draft")
    public Result<SubmissionVO> saveDraft(@PathVariable Long courseId,
                                          @PathVariable Long assignmentId,
                                          @Valid @RequestBody CreateSubmissionRequestDTO dto,
                                          Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(submissionService.saveDraft(courseId, assignmentId, dto,
                loginUser.getUserId(), roles));
    }

    @GetMapping
    public Result<List<SubmissionVO>> listSubmissions(@PathVariable Long courseId,
                                                      @PathVariable Long assignmentId,
                                                      Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(submissionService.listSubmissions(courseId, assignmentId,
                loginUser.getUserId(), roles));
    }

    @GetMapping("/{submissionId}")
    public Result<SubmissionDetailVO> getSubmissionDetail(@PathVariable Long courseId,
                                                            @PathVariable Long assignmentId,
                                                            @PathVariable Long submissionId,
                                                            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(submissionService.getSubmissionDetail(courseId, assignmentId, submissionId,
                loginUser.getUserId(), roles));
    }

    @PostMapping("/{submissionId}/files")
    public Result<SubmissionFileVO> addSubmissionFile(@PathVariable Long courseId,
                                                      @PathVariable Long assignmentId,
                                                      @PathVariable Long submissionId,
                                                      @RequestParam("file") MultipartFile file,
                                                      Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(submissionService.addSubmissionFile(courseId, assignmentId, submissionId,
                file, loginUser.getUserId(), roles));
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
