package com.example.gradescopespringboot.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusUpdateDTO {

    /**
     * User status: 1 active, 0 disabled
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
