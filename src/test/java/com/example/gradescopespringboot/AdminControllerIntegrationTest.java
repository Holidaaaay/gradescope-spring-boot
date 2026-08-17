package com.example.gradescopespringboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String loginAndGetToken(String username) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"password123\"}", username);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"token\":\"") + 9;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    private Long registerUser(String username) throws Exception {
        String body = String.format(
                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                username, username);
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"userId\":") + 9;
        int end = response.indexOf(",", start);
        if (end == -1) {
            end = response.indexOf("}", start);
        }
        return Long.valueOf(response.substring(start, end).trim());
    }

    @Test
    void adminCanListUsers() throws Exception {
        String token = loginAndGetToken("charlie");

        mockMvc.perform(get("/admin/users?pageSize=2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(2));
    }

    @Test
    void adminCanFilterUsersByRole() throws Exception {
        String token = loginAndGetToken("charlie");

        mockMvc.perform(get("/admin/users?role=ADMIN")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].username").value("charlie"))
                .andExpect(jsonPath("$.data.list[0].roles").isArray())
                .andExpect(jsonPath("$.data.list[0].roles[0]").value("ADMIN"));
    }

    @Test
    void adminCanGetUserById() throws Exception {
        String token = loginAndGetToken("charlie");

        mockMvc.perform(get("/admin/users/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").isString())
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void adminCanDisableUser_andDisabledUserCannotLogin() throws Exception {
        String adminToken = loginAndGetToken("charlie");
        String username = "disabletest" + System.currentTimeMillis();
        Long userId = registerUser(username);

        mockMvc.perform(patch("/admin/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\",\"password\":\"password123\"}", username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentCannotAccessAdminEndpoints() throws Exception {
        String token = loginAndGetToken("alice");

        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void dashboardStatsAreAccessibleToAdmin() throws Exception {
        String token = loginAndGetToken("charlie");

        mockMvc.perform(get("/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.totalCourses").isNumber())
                .andExpect(jsonPath("$.data.totalAssignments").isNumber())
                .andExpect(jsonPath("$.data.totalSubmissions").isNumber());
    }
}
