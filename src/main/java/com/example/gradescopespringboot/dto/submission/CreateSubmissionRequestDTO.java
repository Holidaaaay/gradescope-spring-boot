package com.example.gradescopespringboot.dto.submission;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSubmissionRequestDTO {

    @Size(max = 5000, message = "提交文本内容最长 5000 个字符")
    private String contentText;
}
