package com.example.gradescopespringboot.vo.course;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CourseDetailVO {

    private Long id;
    private String courseCode;
    private String courseName;
    private String semester;
    private String description;
    private String location;
    private String scheduleInfo;
    private Integer status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CourseMemberVO> members;
}
