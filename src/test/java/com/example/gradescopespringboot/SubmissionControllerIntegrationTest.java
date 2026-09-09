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
class SubmissionControllerIntegrationTest {

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

    private Long createAssignment(String token, Long courseId, String title, LocalDateTime dueTime,
                                  boolean allowLate, int maxSubmissions) throws Exception {
        String due = dueTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String body = String.format(
                "{\"title\":\"%s\",\"description\":\"desc\",\"totalScore\":100,\"dueTime\":\"%s\",\"allowLateSubmission\":%b,\"maxSubmissionTimes\":%d}",
                title, due, allowLate, maxSubmissions);
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

    private void publishAssignment(String token, Long courseId, Long assignmentId) throws Exception {
        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Long submit(String token, Long courseId, Long assignmentId, String text) throws Exception {
        MvcResult result = mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"contentText\":\"%s\"}", text)))
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
    void studentCanSubmitAssignment() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "SUB" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "HW" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59), false, 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"My answer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.submissionNo").value(1))
                .andExpect(jsonPath("$.data.status").value(1));
    }

    @Test
    void studentCannotSubmitMoreThanMaxTimes() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "MAX" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Max" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59), false, 2);
        publishAssignment(instructor.token(), courseId, assignmentId);

        submit(student.token(), courseId, assignmentId, "first");
        submit(student.token(), courseId, assignmentId, "second");

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"third\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void lateSubmissionRejectedWhenNotAllowed() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "LATE0" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Late" + System.currentTimeMillis(),
                LocalDateTime.now().plusSeconds(2), false, 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        Thread.sleep(2100);

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"late\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void lateSubmissionAcceptedWhenAllowed() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "LATE1" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "LateOk" + System.currentTimeMillis(),
                LocalDateTime.now().plusSeconds(2), true, 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        Thread.sleep(2100);

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"late but ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isLate").value(true));
    }

    @Test
    void draftSubmissionDoesNotCountTowardsMax() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "DRA" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Draft" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59), false, 1);
        publishAssignment(instructor.token(), courseId, assignmentId);

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions/draft")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"draft\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0));

        submit(student.token(), courseId, assignmentId, "final");

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"over\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void instructorCanListAllSubmissionsStudentOnlyOwn() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student1 = login("alice");
        String username = "student2" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());
        AuthInfo student2 = login(username);

        String code = "LIST" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student1.userId());
        enrollStudent(instructor.token(), courseId, student2.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "List" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59), false, 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        submit(student1.token(), courseId, assignmentId, "s1");
        submit(student2.token(), courseId, assignmentId, "s2");

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student1.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].studentId").value(student1.userId()));
    }

    @Test
    void studentCannotViewPeerSubmission() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student1 = login("alice");
        String username = "peer" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());
        AuthInfo student2 = login(username);

        String code = "PEER" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student1.userId());
        enrollStudent(instructor.token(), courseId, student2.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Peer" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59), false, 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        Long submissionId = submit(student1.token(), courseId, assignmentId, "private");

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions/" + submissionId)
                        .header("Authorization", "Bearer " + student2.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void nonMemberCannotSubmit() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "OUT" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        Long assignmentId = createAssignment(instructor.token(), courseId, "Out" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59), false, 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        mockMvc.perform(post("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions")
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentText\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
