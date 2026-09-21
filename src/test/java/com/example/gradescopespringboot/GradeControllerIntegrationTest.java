package com.example.gradescopespringboot;

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
class GradeControllerIntegrationTest {

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
                "{\"courseCode\":\"%s\",\"courseName\":\"Grade Course\",\"semester\":\"%s\",\"description\":\"desc\"}",
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

    private Long createAssignment(String token, Long courseId, String title, int totalScore,
                                  LocalDateTime dueTime, int maxSubmissions) throws Exception {
        String due = dueTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        String body = String.format(
                "{\"title\":\"%s\",\"description\":\"desc\",\"totalScore\":%d,\"dueTime\":\"%s\",\"allowLateSubmission\":false,\"maxSubmissionTimes\":%d}",
                title, totalScore, due, maxSubmissions);
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

    private void gradeSubmission(String token, Long courseId, Long assignmentId, Long submissionId,
                                 double score) throws Exception {
        mockMvc.perform(post(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"score\":%.2f,\"comment\":\"good\"}", score)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private String gradeUrl(Long courseId, Long assignmentId, Long submissionId) {
        return "/courses/" + courseId + "/assignments/" + assignmentId
                + "/submissions/" + submissionId + "/grade";
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
    void instructorCanGradeSubmissionAndSubmissionStatusBecomesGraded() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "GRD" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "HW" + System.currentTimeMillis(),
                100, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);
        Long submissionId = submit(student.token(), courseId, assignmentId, "my work");

        mockMvc.perform(post(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":88.50,\"comment\":\"well done\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(88.5))
                .andExpect(jsonPath("$.data.status").value(1));

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/submissions/" + submissionId)
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(3));
    }

    @Test
    void duplicateGradeRejected() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "DUP" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Dup" + System.currentTimeMillis(),
                100, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);
        Long submissionId = submit(student.token(), courseId, assignmentId, "work");

        gradeSubmission(instructor.token(), courseId, assignmentId, submissionId, 70);

        mockMvc.perform(post(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void scoreExceedingTotalScoreRejected() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "OVR" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Over" + System.currentTimeMillis(),
                60, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);
        Long submissionId = submit(student.token(), courseId, assignmentId, "work");

        mockMvc.perform(post(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":75}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void studentCannotGrade() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "STG" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Stu" + System.currentTimeMillis(),
                100, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);
        Long submissionId = submit(student.token(), courseId, assignmentId, "work");

        mockMvc.perform(post(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + student.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":90}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void studentCanViewOwnGradeButNotPeerGrade() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student1 = login("alice");
        String username = "gradepeer" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());
        AuthInfo student2 = login(username);

        String code = "PEERGRD" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student1.userId());
        enrollStudent(instructor.token(), courseId, student2.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Peer" + System.currentTimeMillis(),
                100, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        Long submission1 = submit(student1.token(), courseId, assignmentId, "s1 work");
        Long submission2 = submit(student2.token(), courseId, assignmentId, "s2 work");
        gradeSubmission(instructor.token(), courseId, assignmentId, submission1, 85);
        gradeSubmission(instructor.token(), courseId, assignmentId, submission2, 95);

        mockMvc.perform(get(gradeUrl(courseId, assignmentId, submission1))
                        .header("Authorization", "Bearer " + student1.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(85.0))
                .andExpect(jsonPath("$.data.studentId").value(student1.userId()));

        mockMvc.perform(get(gradeUrl(courseId, assignmentId, submission2))
                        .header("Authorization", "Bearer " + student1.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void draftGradeHiddenFromStudentUntilFinalized() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "DRAFTG" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Draft" + System.currentTimeMillis(),
                100, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);
        Long submissionId = submit(student.token(), courseId, assignmentId, "work");

        mockMvc.perform(post(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":60,\"comment\":\"draft\",\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(0));

        // draft grade is invisible to the student
        mockMvc.perform(get(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // final grade is still updatable without regrade (it is a draft)
        mockMvc.perform(put(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":75,\"comment\":\"final\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value(1));

        mockMvc.perform(get(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(75.0));

        // a final grade cannot be modified without the regrade flag
        mockMvc.perform(put(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":80}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(409));

        // with the regrade flag it can
        mockMvc.perform(put(gradeUrl(courseId, assignmentId, submissionId))
                        .header("Authorization", "Bearer " + instructor.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":80,\"regrade\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(80.0));
    }

    @Test
    void gradeStatisticsAreAccurateAndStaffOnly() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student1 = login("alice");
        String username = "statstudent" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());
        AuthInfo student2 = login(username);

        String code = "STAT" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student1.userId());
        enrollStudent(instructor.token(), courseId, student2.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "Stat" + System.currentTimeMillis(),
                100, LocalDateTime.of(2026, 12, 31, 23, 59, 59), 3);
        publishAssignment(instructor.token(), courseId, assignmentId);

        Long submission1 = submit(student1.token(), courseId, assignmentId, "s1");
        Long submission2 = submit(student2.token(), courseId, assignmentId, "s2");
        gradeSubmission(instructor.token(), courseId, assignmentId, submission1, 80);
        gradeSubmission(instructor.token(), courseId, assignmentId, submission2, 90);

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/grades/statistics")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.gradedCount").value(2))
                .andExpect(jsonPath("$.data.averageScore").value(85.0))
                .andExpect(jsonPath("$.data.medianScore").value(85.0))
                .andExpect(jsonPath("$.data.maxScore").value(90.0))
                .andExpect(jsonPath("$.data.minScore").value(80.0));

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId + "/grades/statistics")
                        .header("Authorization", "Bearer " + student1.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }
}
