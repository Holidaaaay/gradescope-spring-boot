package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    List<UserRole> selectByUserId(Long userId);

    int insert(UserRole userRole);

    int deleteByUserId(Long userId);
}
