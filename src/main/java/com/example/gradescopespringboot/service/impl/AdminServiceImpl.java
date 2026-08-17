package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.result.PageResult;
import com.example.gradescopespringboot.entity.Role;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.entity.UserRole;
import com.example.gradescopespringboot.mapper.UserMapper;
import com.example.gradescopespringboot.service.AdminService;
import com.example.gradescopespringboot.service.RoleService;
import com.example.gradescopespringboot.service.UserRoleService;
import com.example.gradescopespringboot.vo.admin.AdminUserListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserMapper userMapper;
    private final UserRoleService userRoleService;
    private final RoleService roleService;

    public AdminServiceImpl(UserMapper userMapper,
                            UserRoleService userRoleService,
                            RoleService roleService) {
        this.userMapper = userMapper;
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    public PageResult<AdminUserListVO> listUsers(Integer pageNum, Integer pageSize, String role, Integer status) {
        int effectivePageNum = (pageNum == null || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        int effectivePageSize = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        int offset = (effectivePageNum - 1) * effectivePageSize;

        Long total = userMapper.countAdminUsers(role, status);
        List<AdminUserListVO> list = userMapper.selectAdminUserList(role, status, offset, effectivePageSize);
        list.forEach(this::applyRoleCodes);

        return new PageResult<>(list, total, effectivePageNum, effectivePageSize);
    }

    @Override
    public AdminUserListVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        return toAdminUserListVO(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("Status must be 0 or 1");
        }
        userMapper.updateStatusById(id, status);
    }

    @Override
    public Map<String, Long> dashboardStats() {
        return Map.of(
                "totalUsers", Optional.ofNullable(userMapper.countUsers()).orElse(0L),
                "totalCourses", Optional.ofNullable(userMapper.countCourses()).orElse(0L),
                "totalAssignments", Optional.ofNullable(userMapper.countAssignments()).orElse(0L),
                "totalSubmissions", Optional.ofNullable(userMapper.countSubmissions()).orElse(0L)
        );
    }

    private AdminUserListVO toAdminUserListVO(User user) {
        AdminUserListVO vo = new AdminUserListVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setUserNo(user.getUserNo());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());

        List<String> roleCodes = userRoleService.getByUserId(user.getId()).stream()
                .map(UserRole::getRoleId)
                .map(roleService::getById)
                .flatMap(Optional::stream)
                .map(Role::getRoleCode)
                .toList();
        vo.setRoles(roleCodes);

        return vo;
    }

    private void applyRoleCodes(AdminUserListVO vo) {
        String roleCodes = vo.getRoleCodes();
        if (roleCodes == null || roleCodes.isBlank()) {
            vo.setRoles(Collections.emptyList());
            return;
        }
        vo.setRoles(List.of(roleCodes.split(",")));
    }
}
