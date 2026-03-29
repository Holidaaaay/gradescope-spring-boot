package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.entity.User;

public interface UserService {

    /**
     * Get a user by id
     *
     * @param id user id
     * @return user entity
     */
    User getById(Long id);

    /**
     * Get a user by username
     *
     * @param username user id
     * @return user entity
     */
    User getByUsername(String username);

    /**
     * 新用户存储至数据库
     *
     * @param user user object
     */
    void save(User user);
}
