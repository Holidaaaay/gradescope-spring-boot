package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.course.CreateCourseRequestDTO;
import com.example.gradescopespringboot.dto.course.EnrollMemberRequestDTO;
import com.example.gradescopespringboot.dto.course.UpdateCourseRequestDTO;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.CourseService;
import com.example.gradescopespringboot.vo.course.CourseDetailVO;
import com.example.gradescopespringboot.vo.course.CourseVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public Result<CourseVO> createCourse(@Valid @RequestBody CreateCourseRequestDTO dto,
                                         Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return Result.success(courseService.createCourse(dto, loginUser.getUserId()));
    }

    @GetMapping
    public Result<List<CourseVO>> listCourses(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(courseService.listCourses(loginUser.getUserId(), roles));
    }

    @GetMapping("/{id}")
    public Result<CourseDetailVO> getCourseDetail(@PathVariable Long id,
                                                   Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(courseService.getCourseDetail(id, loginUser.getUserId(), roles));
    }

    @PutMapping("/{id}")
    public Result<CourseVO> updateCourse(@PathVariable Long id,
                                        @Valid @RequestBody UpdateCourseRequestDTO dto,
                                        Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(courseService.updateCourse(id, dto, loginUser.getUserId(), roles));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id,
                                     Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        courseService.deleteCourse(id, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    @PostMapping("/{id}/members")
    public Result<Void> enrollMember(@PathVariable Long id,
                                     @Valid @RequestBody EnrollMemberRequestDTO dto,
                                     Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        courseService.enrollMember(id, dto, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                      @PathVariable Long userId,
                                      Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        courseService.removeMember(id, userId, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
