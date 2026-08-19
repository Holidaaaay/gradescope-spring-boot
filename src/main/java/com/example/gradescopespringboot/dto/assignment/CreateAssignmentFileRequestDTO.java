package com.example.gradescopespringboot.dto.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAssignmentFileRequestDTO {

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名最长 255 个字符")
    private String fileName;

    @NotBlank(message = "文件 URL 不能为空")
    @Size(max = 500, message = "文件 URL 最长 500 个字符")
    private String fileUrl;

    private Long fileSize;

    @Size(max = 100, message = "文件类型最长 100 个字符")
    private String fileType;
}
