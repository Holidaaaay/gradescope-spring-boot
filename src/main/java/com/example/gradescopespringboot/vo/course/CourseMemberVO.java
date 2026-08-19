package com.example.gradescopespringboot.vo.course;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseMemberVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String courseRole;
    private LocalDateTime joinedAt;
}
