package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.entity.Role;
import com.example.gradescopespringboot.mapper.RoleMapper;
import com.example.gradescopespringboot.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public Optional<Role> getByCode(String roleCode) {
        return roleMapper.selectByCode(roleCode);
    }

    @Override
    public Optional<Role> getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public List<Role> listActiveRoles() {
        return roleMapper.selectAllActive();
    }

    @Override
    public void save(Role role) {
        roleMapper.insert(role);
    }
}
