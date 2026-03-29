package com.example.gradescopespringboot;

import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(1L);
        System.out.println(user.getEmail());
    }
}
