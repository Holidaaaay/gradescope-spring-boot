package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    /**
     * Get role by code
     *
     * @param roleCode role code
     * @return role entity
     */
    Optional<Role> getByCode(String roleCode);

    /**
     * Get role by id
     *
     * @param id role id
     * @return role entity
     */
    Optional<Role> getById(Long id);

    /**
     * List all active roles
     *
     * @return active roles
     */
    List<Role> listActiveRoles();

    /**
     * Save a role
     *
     * @param role role entity
     */
    void save(Role role);
}
