package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.PageResult;
import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.dto.admin.AdminUserStatusUpdateDTO;
import com.example.gradescopespringboot.service.AdminService;
import com.example.gradescopespringboot.vo.admin.AdminUserListVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public Result<PageResult<AdminUserListVO>> listUsers(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return Result.success(adminService.listUsers(pageNum, pageSize, role, status));
    }

    @GetMapping("/users/{id}")
    public Result<AdminUserListVO> getUserById(@PathVariable Long id) {
        return Result.success(adminService.getUserById(id));
    }

    @PatchMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                          @Valid @RequestBody AdminUserStatusUpdateDTO dto) {
        adminService.updateUserStatus(id, dto.getStatus());
        return Result.success(null);
    }

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Long>> stats() {
        return Result.success(adminService.dashboardStats());
    }
}
