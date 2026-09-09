package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.submission.CreateSubmissionRequestDTO;
import com.example.gradescopespringboot.vo.submission.SubmissionDetailVO;
import com.example.gradescopespringboot.vo.submission.SubmissionVO;

import java.util.List;

public interface SubmissionService {

    /**
     * Create a new submission for an assignment. Only students can submit.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param dto          submission data
     * @param studentId    student user id
     * @param roles        user global roles
     * @return created submission
     */
    SubmissionVO createSubmission(Long courseId, Long assignmentId, CreateSubmissionRequestDTO dto,
                                  Long studentId, List<String> roles);

    /**
     * Save a draft submission. Only students can save drafts.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param dto          submission data
     * @param studentId    student user id
     * @param roles        user global roles
     * @return saved draft submission
     */
    SubmissionVO saveDraft(Long courseId, Long assignmentId, CreateSubmissionRequestDTO dto,
                           Long studentId, List<String> roles);

    /**
     * List submissions for an assignment. Students see their own; instructors/TAs/admins see all.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param userId       user id
     * @param roles        user global roles
     * @return submission list
     */
    List<SubmissionVO> listSubmissions(Long courseId, Long assignmentId, Long userId, List<String> roles);

    /**
     * Get submission detail. Students can only view their own submissions.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param submissionId submission id
     * @param userId       user id
     * @param roles        user global roles
     * @return submission detail
     */
    SubmissionDetailVO getSubmissionDetail(Long courseId, Long assignmentId, Long submissionId,
                                           Long userId, List<String> roles);
}
