package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.entity.UserRole;
import com.example.gradescopespringboot.mapper.UserRoleMapper;
import com.example.gradescopespringboot.service.UserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    public UserRoleServiceImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<UserRole> getByUserId(Long userId) {
        return userRoleMapper.selectByUserId(userId);
    }

    @Override
    public void assignRole(UserRole userRole) {
        userRoleMapper.insert(userRole);
    }

    @Override
    public void removeAllRoles(Long userId) {
        userRoleMapper.deleteByUserId(userId);
    }
}
