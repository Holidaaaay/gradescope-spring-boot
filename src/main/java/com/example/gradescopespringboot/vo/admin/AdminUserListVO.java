package com.example.gradescopespringboot.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserListVO {

    /**
     * User ID
     */
    private Long id;

    /**
     * Login username
     */
    private String username;

    /**
     * Real name
     */
    private String realName;

    /**
     * Email address
     */
    private String email;

    /**
     * Phone number
     */
    private String phone;

    /**
     * User number (student/employee ID)
     */
    private String userNo;

    /**
     * User status: 1 active, 0 disabled
     */
    private Integer status;

    /**
     * User roles
     */
    private List<String> roles;

    /**
     * Comma-separated role codes from SQL GROUP_CONCAT (internal use)
     */
    private String roleCodes;

    /**
     * Created time
     */
    private LocalDateTime createdAt;
}
