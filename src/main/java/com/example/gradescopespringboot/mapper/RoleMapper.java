package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface RoleMapper {

    Optional<Role> selectById(Long id);

    Optional<Role> selectByCode(String roleCode);

    List<Role> selectAllActive();

    int insert(Role role);
}
