package com.example.gradescopespringboot;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private record AuthInfo(String token, Long userId) {
    }

    private AuthInfo login(String username) throws Exception {
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
        String token = response.substring(start, end);

        MvcResult meResult = mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String meResponse = meResult.getResponse().getContentAsString();
        int idStart = meResponse.indexOf("\"userId\":") + 9;
        int idEnd = meResponse.indexOf(",", idStart);
        if (idEnd == -1) {
            idEnd = meResponse.indexOf("}", idStart);
        }
        Long userId = Long.valueOf(meResponse.substring(idStart, idEnd).trim());

        return new AuthInfo(token, userId);
    }

    private Long createCourse(String token, String code, String semester) throws Exception {
        String body = String.format(
                "{\"courseCode\":\"%s\",\"courseName\":\"Test Course\",\"semester\":\"%s\",\"description\":\"desc\"}",
                code, semester);
        MvcResult result = mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"id\":") + 5;
        int end = response.indexOf(",", start);
        if (end == -1) {
            end = response.indexOf("}", start);
        }
        return Long.valueOf(response.substring(start, end).trim());
    }

    @Test
    void instructorCanCreateCourse() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "CS" + System.currentTimeMillis();

        mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"courseCode\":\"%s\",\"courseName\":\"Test\",\"semester\":\"2026 Spring\"}", code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.courseCode").value(code))
                .andExpect(jsonPath("$.data.createdBy").isNumber());
    }

    @Test
    void studentCannotCreateCourse() throws Exception {
        AuthInfo student = login("alice");

        mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseCode\":\"CS101\",\"courseName\":\"Test\",\"semester\":\"2026 Spring\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateCourseCodeAndSemesterReturnsConflict() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "DUP" + System.currentTimeMillis();
        String semester = "2026 Spring";

        createCourse(instructor.token(), code, semester);

        mockMvc.perform(post("/courses")
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"courseCode\":\"%s\",\"courseName\":\"Duplicate\",\"semester\":\"%s\"}", code, semester)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void instructorCanUpdateOwnCourse() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "UPD" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        mockMvc.perform(put("/courses/" + courseId)
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseCode\":\"" + code + "\",\"courseName\":\"Updated Name\",\"semester\":\"2026 Spring\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.courseName").value("Updated Name"));
    }

    @Test
    void enrolledStudentCanListCourse() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "ENR" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        mockMvc.perform(post("/courses/" + courseId + "/members")
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"userId\":%d,\"courseRole\":\"STUDENT\"}", student.userId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/courses")
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].courseCode").value(Matchers.hasItem(code)));
    }

    @Test
    void courseDetailShowsAllMembersToInstructor_andOnlySelfToStudent() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "DET" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        mockMvc.perform(post("/courses/" + courseId + "/members")
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"userId\":%d,\"courseRole\":\"STUDENT\"}", student.userId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/courses/" + courseId)
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.members.length()").value(2));

        mockMvc.perform(get("/courses/" + courseId)
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.members.length()").value(1))
                .andExpect(jsonPath("$.data.members[0].userId").value(student.userId()));
    }

    @Test
    void nonMemberCannotAccessCourseDetail() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "PRIV" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        String username = "outsider" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());

        AuthInfo outsider = login(username);
        mockMvc.perform(get("/courses/" + courseId)
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
