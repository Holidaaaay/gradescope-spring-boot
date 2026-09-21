package com.example.gradescopespringboot.dto.grade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateGradeRequestDTO {

    @NotNull(message = "分数不能为空")
    @DecimalMin(value = "0.0", message = "分数不能为负数")
    private BigDecimal score;

    @Size(max = 2000, message = "评语最长 2000 个字符")
    private String comment;

    /**
     * 0 = draft, 1 = final. Defaults to final when omitted.
     */
    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    private Integer status;
}
