package com.example.gradescopespringboot.controller;

import com.example.gradescopespringboot.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Long>> stats() {
        return Result.success(Map.of(
                "totalUsers", 0L,
                "totalCourses", 0L,
                "totalAssignments", 0L,
                "totalSubmissions", 0L
        ));
    }
}
