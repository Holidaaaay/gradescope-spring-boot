package com.example.gradescopespringboot.dto.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadMaterialRequestDTO {

    @NotBlank(message = "资料标题不能为空")
    @Size(max = 200, message = "资料标题最长 200 个字符")
    private String title;

    private String description;

    private MultipartFile file;
}
