package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.grade.CreateGradeRequestDTO;
import com.example.gradescopespringboot.dto.grade.UpdateGradeRequestDTO;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.GradeService;
import com.example.gradescopespringboot.vo.grade.GradeStatisticsVO;
import com.example.gradescopespringboot.vo.grade.GradeVO;
import com.example.gradescopespringboot.vo.grade.GradeWithSubmissionVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses/{courseId}/assignments/{assignmentId}")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public Result<GradeVO> createGrade(@PathVariable Long courseId,
                                       @PathVariable Long assignmentId,
                                       @PathVariable Long submissionId,
                                       @Valid @RequestBody CreateGradeRequestDTO dto,
                                       Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(gradeService.createGrade(courseId, assignmentId, submissionId, dto,
                loginUser.getUserId(), extractRoles(authentication)));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public Result<GradeVO> updateGrade(@PathVariable Long courseId,
                                       @PathVariable Long assignmentId,
                                       @PathVariable Long submissionId,
                                       @Valid @RequestBody UpdateGradeRequestDTO dto,
                                       Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(gradeService.updateGrade(courseId, assignmentId, submissionId, dto,
                loginUser.getUserId(), extractRoles(authentication)));
    }

    @GetMapping("/submissions/{submissionId}/grade")
    public Result<GradeWithSubmissionVO> getGradeBySubmission(@PathVariable Long courseId,
                                                              @PathVariable Long assignmentId,
                                                              @PathVariable Long submissionId,
                                                              Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(gradeService.getGradeBySubmission(courseId, assignmentId, submissionId,
                loginUser.getUserId(), extractRoles(authentication)));
    }

    @GetMapping("/grades/statistics")
    public Result<GradeStatisticsVO> getGradeStatistics(@PathVariable Long courseId,
                                                        @PathVariable Long assignmentId,
                                                        Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(gradeService.getGradeStatistics(courseId, assignmentId,
                loginUser.getUserId(), extractRoles(authentication)));
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
