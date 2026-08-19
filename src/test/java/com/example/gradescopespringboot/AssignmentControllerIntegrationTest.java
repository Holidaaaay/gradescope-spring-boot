package com.example.gradescopespringboot;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentControllerIntegrationTest {

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
        String token = extractJsonValue(response, "\"token\":\"");

        MvcResult meResult = mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String meResponse = meResult.getResponse().getContentAsString();
        Long userId = Long.valueOf(extractJsonValue(meResponse, "\"userId\":").trim());

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
        return Long.valueOf(extractJsonValue(response, "\"id\":").trim());
    }

    private void enrollStudent(String token, Long courseId, Long studentId) throws Exception {
        mockMvc.perform(post("/courses/" + courseId + "/members")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"userId\":%d,\"courseRole\":\"STUDENT\"}", studentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Long createAssignment(String token, Long courseId, String title, LocalDateTime dueTime) throws Exception {
        String due = dueTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String body = String.format(
                "{\"title\":\"%s\",\"description\":\"desc\",\"totalScore\":100,\"dueTime\":\"%s\",\"allowLateSubmission\":false,\"maxSubmissionTimes\":3}",
                title, due);
        MvcResult result = mockMvc.perform(post("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return Long.valueOf(extractJsonValue(response, "\"id\":").trim());
    }

    private String extractJsonValue(String response, String key) {
        int start = response.indexOf(key) + key.length();
        int end = response.indexOf(",", start);
        if (end == -1) {
            end = response.indexOf("}", start);
        }
        if (end == -1) {
            end = response.indexOf("]", start);
        }
        return response.substring(start, end);
    }

    @Test
    void instructorCanCreateAssignment() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "ASG" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        String title = "Homework " + System.currentTimeMillis();

        mockMvc.perform(post("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"title\":\"%s\",\"description\":\"desc\",\"totalScore\":100,\"dueTime\":\"2026-12-31T23:59:59\",\"allowLateSubmission\":false,\"maxSubmissionTimes\":3}",
                                title)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value(title))
                .andExpect(jsonPath("$.data.status").value(0))
                .andExpect(jsonPath("$.data.courseId").value(courseId));
    }

    @Test
    void studentCannotCreateAssignment() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "STU" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        mockMvc.perform(post("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Hack\",\"description\":\"x\",\"totalScore\":100,\"dueTime\":\"2026-12-31T23:59:59\",\"allowLateSubmission\":false,\"maxSubmissionTimes\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void instructorCanPublishAndStudentSeesIt() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "PUB" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());

        String title = "Published " + System.currentTimeMillis();
        Long assignmentId = createAssignment(instructor.token(), courseId, title,
                LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        mockMvc.perform(get("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[*].title").value(Matchers.not(Matchers.hasItem(title))));

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/publish")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].title").value(Matchers.hasItem(title)));
    }

    @Test
    void instructorCanUpdateAssignmentBeforeDue() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "UPD" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        String title = "Original " + System.currentTimeMillis();
        Long assignmentId = createAssignment(instructor.token(), courseId, title,
                LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        mockMvc.perform(put("/courses/" + courseId + "/assignments/" + assignmentId)
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\",\"totalScore\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.totalScore").value(80));
    }

    @Test
    void cannotUpdateAssignmentAfterDueWhenPublished() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "LATE" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        String title = "DueSoon " + System.currentTimeMillis();
        Long assignmentId = createAssignment(instructor.token(), courseId, title,
                LocalDateTime.now().plusSeconds(2));

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/publish")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Thread.sleep(2100);

        mockMvc.perform(put("/courses/" + courseId + "/assignments/" + assignmentId)
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Too Late\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void instructorCanCloseAssignment() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "CLO" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());

        String title = "CloseMe " + System.currentTimeMillis();
        Long assignmentId = createAssignment(instructor.token(), courseId, title,
                LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/publish")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/close")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].title").value(Matchers.not(Matchers.hasItem(title))));
    }

    @Test
    void nonMemberCannotAccessAssignments() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "OUT" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        String username = "outsider" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());

        AuthInfo outsider = login(username);
        mockMvc.perform(get("/courses/" + courseId + "/assignments")
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void addAndListAssignmentFilePlaceholder() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "FILE" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        String title = "FileTask " + System.currentTimeMillis();
        Long assignmentId = createAssignment(instructor.token(), courseId, title,
                LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/files")
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"hw.pdf\",\"fileUrl\":\"/uploads/hw.pdf\",\"fileSize\":1024,\"fileType\":\"pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileName").value("hw.pdf"));

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/files")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
