package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.mapper.UserMapper;
import com.example.gradescopespringboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public void save(User user) {
        userMapper.insert(user);
    }

}
