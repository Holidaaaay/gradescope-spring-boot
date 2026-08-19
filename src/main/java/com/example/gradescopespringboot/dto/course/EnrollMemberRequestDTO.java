package com.example.gradescopespringboot.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EnrollMemberRequestDTO {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    @NotBlank(message = "课程角色不能为空")
    @Pattern(regexp = "STUDENT|TA|INSTRUCTOR", message = "课程角色必须是 STUDENT、TA 或 INSTRUCTOR")
    private String courseRole;
}
