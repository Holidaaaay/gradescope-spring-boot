package com.example.gradescopespringboot;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FileStorageIntegrationTest {

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
        int end;
        if (key.endsWith("\":\"")) {
            end = response.indexOf("\"", start);
        } else {
            end = response.indexOf(",", start);
            if (end == -1) {
                end = response.indexOf("}", start);
            }
        }
        if (end == -1) {
            end = response.length();
        }
        return response.substring(start, end);
    }

    @Test
    void teacherCanUploadCourseMaterialAndStudentCanList() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "MAT" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());

        MockMultipartFile file = new MockMultipartFile(
                "file", "slides.pdf", "application/pdf", "pdf content".getBytes());

        mockMvc.perform(multipart("/courses/" + courseId + "/materials")
                        .file(file)
                        .param("title", "Week 1 Slides")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Week 1 Slides"))
                .andExpect(jsonPath("$.data.fileName").value("slides.pdf"));

        mockMvc.perform(get("/courses/" + courseId + "/materials")
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[*].title").value(Matchers.hasItem("Week 1 Slides")));
    }

    @Test
    void studentCanUploadSubmissionFile() throws Exception {
        AuthInfo instructor = login("bob");
        AuthInfo student = login("alice");
        String code = "SUBF" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");
        enrollStudent(instructor.token(), courseId, student.userId());
        Long assignmentId = createAssignment(instructor.token(), courseId, "WithFile" + System.currentTimeMillis(),
                LocalDateTime.of(2026, 12, 31, 23, 59, 59));
        publishAssignment(instructor.token(), courseId, assignmentId);
        Long submissionId = submit(student.token(), courseId, assignmentId, "answer");

        MockMultipartFile zip = new MockMultipartFile(
                "file", "answer.zip", "application/zip", "zip bytes".getBytes());

        mockMvc.perform(multipart("/courses/" + courseId + "/assignments/" + assignmentId
                                + "/submissions/" + submissionId + "/files")
                        .file(zip)
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileName").value("answer.zip"));

        mockMvc.perform(get("/courses/" + courseId + "/assignments/" + assignmentId
                                + "/submissions/" + submissionId)
                        .header("Authorization", "Bearer " + student.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.files").isArray())
                .andExpect(jsonPath("$.data.files.length()").value(1));
    }

    @Test
    void courseMemberCanDownloadFile() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "DL" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "hello file".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/courses/" + courseId + "/materials")
                        .file(file)
                        .param("title", "Notes")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andReturn();

        String response = uploadResult.getResponse().getContentAsString();
        String fileUrl = extractJsonValue(response, "\"fileUrl\":\"");

        mockMvc.perform(get(fileUrl)
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(content().string("hello file"));
    }

    @Test
    void nonMemberCannotDownloadFile() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "DL403" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        MockMultipartFile file = new MockMultipartFile(
                "file", "secret.txt", "text/plain", "secret".getBytes());

        MvcResult uploadResult = mockMvc.perform(multipart("/courses/" + courseId + "/materials")
                        .file(file)
                        .param("title", "Secret")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andReturn();

        String response = uploadResult.getResponse().getContentAsString();
        String fileUrl = extractJsonValue(response, "\"fileUrl\":\"");

        String username = "outsider" + System.currentTimeMillis();
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format(
                                "{\"username\":\"%s\",\"password\":\"password123\",\"email\":\"%s@example.com\"}",
                                username, username)))
                .andExpect(status().isOk());
        AuthInfo outsider = login(username);

        mockMvc.perform(get(fileUrl)
                        .header("Authorization", "Bearer " + outsider.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void exeUploadRejected() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "EXE" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        MockMultipartFile evil = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "MZ".getBytes());

        mockMvc.perform(multipart("/courses/" + courseId + "/materials")
                        .file(evil)
                        .param("title", "Virus")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void oversizedFileRejected() throws Exception {
        AuthInfo instructor = login("bob");
        String code = "BIG" + System.currentTimeMillis();
        Long courseId = createCourse(instructor.token(), code, "2026 Spring");

        byte[] largeContent = new byte[11 * 1024 * 1024];
        MockMultipartFile big = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", largeContent);

        mockMvc.perform(multipart("/courses/" + courseId + "/materials")
                        .file(big)
                        .param("title", "Too Big")
                        .header("Authorization", "Bearer " + instructor.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
