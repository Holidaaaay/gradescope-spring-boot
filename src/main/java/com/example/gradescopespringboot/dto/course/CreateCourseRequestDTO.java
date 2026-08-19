package com.example.gradescopespringboot.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCourseRequestDTO {

    @NotBlank(message = "课程编号不能为空")
    @Size(max = 50, message = "课程编号最长 50 个字符")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 200, message = "课程名称最长 200 个字符")
    private String courseName;

    @NotBlank(message = "学期不能为空")
    @Size(max = 50, message = "学期最长 50 个字符")
    private String semester;

    @Size(max = 200, message = "上课地点最长 200 个字符")
    private String location;

    @Size(max = 255, message = "上课时间信息最长 255 个字符")
    private String scheduleInfo;

    private String description;
}
