package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.entity.User;

public interface UserService {

    /**
     * Get a user by id
     *
     * @param id user id
     * @return user entity
     */
    User getById(Long id);
}
