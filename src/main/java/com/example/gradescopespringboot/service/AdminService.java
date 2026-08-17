package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.common.result.PageResult;
import com.example.gradescopespringboot.vo.admin.AdminUserListVO;

import java.util.Map;

public interface AdminService {

    /**
     * Paginated user list for admin with optional role/status filters.
     *
     * @param pageNum  page number (1-based)
     * @param pageSize page size
     * @param role     role code filter
     * @param status   user status filter
     * @return paginated user list
     */
    PageResult<AdminUserListVO> listUsers(Integer pageNum, Integer pageSize, String role, Integer status);

    /**
     * Get user details for admin view.
     *
     * @param id user id
     * @return user details
     */
    AdminUserListVO getUserById(Long id);

    /**
     * Update user status (enable/disable).
     *
     * @param id     user id
     * @param status 1 active, 0 disabled
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * Dashboard statistics.
     *
     * @return counts map
     */
    Map<String, Long> dashboardStats();
}
