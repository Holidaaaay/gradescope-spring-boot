package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.service.UserService;
import com.example.gradescopespringboot.vo.user.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return Result.success(userVO);
    }
}
