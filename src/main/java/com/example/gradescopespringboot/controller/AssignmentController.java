package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.assignment.CreateAssignmentFileRequestDTO;
import com.example.gradescopespringboot.dto.assignment.CreateAssignmentRequestDTO;
import com.example.gradescopespringboot.dto.assignment.UpdateAssignmentRequestDTO;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.AssignmentService;
import com.example.gradescopespringboot.vo.assignment.AssignmentDetailVO;
import com.example.gradescopespringboot.vo.assignment.AssignmentFileVO;
import com.example.gradescopespringboot.vo.assignment.AssignmentVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses/{courseId}/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public Result<AssignmentVO> createAssignment(@PathVariable Long courseId,
                                                   @Valid @RequestBody CreateAssignmentRequestDTO dto,
                                                   Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(assignmentService.createAssignment(courseId, dto, loginUser.getUserId(), roles));
    }

    @GetMapping
    public Result<List<AssignmentVO>> listAssignments(@PathVariable Long courseId,
                                                      Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(assignmentService.listAssignmentsByCourse(courseId, loginUser.getUserId(), roles));
    }

    @GetMapping("/{assignmentId}")
    public Result<AssignmentDetailVO> getAssignmentDetail(@PathVariable Long courseId,
                                                            @PathVariable Long assignmentId,
                                                            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(assignmentService.getAssignmentDetail(courseId, assignmentId,
                loginUser.getUserId(), roles));
    }

    @PutMapping("/{assignmentId}")
    public Result<AssignmentVO> updateAssignment(@PathVariable Long courseId,
                                                   @PathVariable Long assignmentId,
                                                   @Valid @RequestBody UpdateAssignmentRequestDTO dto,
                                                   Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(assignmentService.updateAssignment(courseId, assignmentId, dto,
                loginUser.getUserId(), roles));
    }

    @PostMapping("/{assignmentId}/publish")
    public Result<Void> publishAssignment(@PathVariable Long courseId,
                                           @PathVariable Long assignmentId,
                                           Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        assignmentService.publishAssignment(courseId, assignmentId, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    @PostMapping("/{assignmentId}/close")
    public Result<Void> closeAssignment(@PathVariable Long courseId,
                                         @PathVariable Long assignmentId,
                                         Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        assignmentService.closeAssignment(courseId, assignmentId, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    @DeleteMapping("/{assignmentId}")
    public Result<Void> deleteAssignment(@PathVariable Long courseId,
                                          @PathVariable Long assignmentId,
                                          Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        assignmentService.deleteAssignment(courseId, assignmentId, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    @PostMapping("/{assignmentId}/files")
    public Result<AssignmentFileVO> addAssignmentFile(@PathVariable Long courseId,
                                                       @PathVariable Long assignmentId,
                                                       @Valid @RequestBody CreateAssignmentFileRequestDTO dto,
                                                       Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(assignmentService.addAssignmentFile(courseId, assignmentId, dto,
                loginUser.getUserId(), roles));
    }

    @GetMapping("/{assignmentId}/files")
    public Result<List<AssignmentFileVO>> listAssignmentFiles(@PathVariable Long courseId,
                                                               @PathVariable Long assignmentId,
                                                               Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(assignmentService.listAssignmentFiles(courseId, assignmentId,
                loginUser.getUserId(), roles));
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
