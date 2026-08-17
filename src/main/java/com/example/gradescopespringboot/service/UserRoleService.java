package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.entity.UserRole;

import java.util.List;

public interface UserRoleService {

    /**
     * Get role associations for a user
     *
     * @param userId user id
     * @return user role list
     */
    List<UserRole> getByUserId(Long userId);

    /**
     * Assign a role to a user
     *
     * @param userRole user role association
     */
    void assignRole(UserRole userRole);

    /**
     * Remove all roles from a user
     *
     * @param userId user id
     */
    void removeAllRoles(Long userId);
}
