package com.example.gradescopespringboot.dto.assignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateAssignmentRequestDTO {

    @Size(max = 200, message = "作业标题最长 200 个字符")
    private String title;

    private String description;

    @DecimalMin(value = "0.01", message = "总分必须大于 0")
    @DecimalMax(value = "9999.99", message = "总分超出允许范围")
    private BigDecimal totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueTime;

    private Boolean allowLateSubmission;

    @Min(value = 1, message = "最大提交次数至少为 1")
    @Max(value = 100, message = "最大提交次数不能超过 100")
    private Integer maxSubmissionTimes;
}
