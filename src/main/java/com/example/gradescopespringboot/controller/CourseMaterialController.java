package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.material.UploadMaterialRequestDTO;
import com.example.gradescopespringboot.security.model.LoginUser;
import com.example.gradescopespringboot.service.CourseMaterialService;
import com.example.gradescopespringboot.vo.material.CourseMaterialVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/courses/{courseId}/materials")
public class CourseMaterialController {

    private final CourseMaterialService courseMaterialService;

    public CourseMaterialController(CourseMaterialService courseMaterialService) {
        this.courseMaterialService = courseMaterialService;
    }

    @PostMapping
    public Result<CourseMaterialVO> uploadMaterial(@PathVariable Long courseId,
                                                   @Valid @ModelAttribute UploadMaterialRequestDTO dto,
                                                   Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(courseMaterialService.uploadMaterial(courseId, dto,
                loginUser.getUserId(), roles));
    }

    @GetMapping
    public Result<List<CourseMaterialVO>> listMaterials(@PathVariable Long courseId,
                                                        Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        return Result.success(courseMaterialService.listMaterialsByCourse(courseId,
                loginUser.getUserId(), roles));
    }

    @DeleteMapping("/{materialId}")
    public Result<Void> deleteMaterial(@PathVariable Long courseId,
                                       @PathVariable Long materialId,
                                       Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        List<String> roles = extractRoles(authentication);
        courseMaterialService.deleteMaterial(courseId, materialId, loginUser.getUserId(), roles);
        return Result.success(null);
    }

    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
