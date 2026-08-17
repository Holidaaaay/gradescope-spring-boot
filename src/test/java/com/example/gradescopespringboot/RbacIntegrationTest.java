package com.example.gradescopespringboot;

import com.example.gradescopespringboot.security.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RbacIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        String token = loginAndGetToken("charlie");

        mockMvc.perform(get("/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void studentCannotAccessAdminEndpoint() throws Exception {
        String token = loginAndGetToken("alice");

        mockMvc.perform(get("/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void authMeReturnsRoles() throws Exception {
        String token = loginAndGetToken("bob");

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("bob"))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles[0]").value("INSTRUCTOR"));
    }

    @Test
    void tokenContainsRolesClaim() throws Exception {
        String token = loginAndGetToken("charlie");

        Claims claims = jwtTokenProvider.parseToken(token);
        assertThat(claims.get("roles")).asList().contains("ADMIN");
    }
}
