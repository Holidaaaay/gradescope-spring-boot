package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    User selectById(Long id);

    User selectByUsername(String username);

    int insert(User user);

}
