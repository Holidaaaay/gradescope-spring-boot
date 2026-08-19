package com.example.gradescopespringboot.dto.assignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateAssignmentRequestDTO {

    @NotBlank(message = "作业标题不能为空")
    @Size(max = 200, message = "作业标题最长 200 个字符")
    private String title;

    private String description;

    @NotNull(message = "总分不能为空")
    @DecimalMin(value = "0.01", message = "总分必须大于 0")
    @DecimalMax(value = "9999.99", message = "总分超出允许范围")
    private BigDecimal totalScore;

    @NotNull(message = "截止时间不能为空")
    @Future(message = "截止时间必须是未来时间")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueTime;

    @NotNull(message = "是否允许迟交不能为空")
    private Boolean allowLateSubmission;

    @NotNull(message = "最大提交次数不能为空")
    @Min(value = 1, message = "最大提交次数至少为 1")
    @Max(value = 100, message = "最大提交次数不能超过 100")
    private Integer maxSubmissionTimes;
}
