package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.assignment.CreateAssignmentFileRequestDTO;
import com.example.gradescopespringboot.dto.assignment.CreateAssignmentRequestDTO;
import com.example.gradescopespringboot.dto.assignment.UpdateAssignmentRequestDTO;
import com.example.gradescopespringboot.vo.assignment.AssignmentDetailVO;
import com.example.gradescopespringboot.vo.assignment.AssignmentFileVO;
import com.example.gradescopespringboot.vo.assignment.AssignmentVO;

import java.util.List;

public interface AssignmentService {

    /**
     * Create a new assignment within a course. Default status is draft (0).
     *
     * @param courseId course id
     * @param dto      assignment data
     * @param userId   operator user id
     * @param roles    operator global roles
     * @return created assignment
     */
    AssignmentVO createAssignment(Long courseId, CreateAssignmentRequestDTO dto, Long userId, List<String> roles);

    /**
     * Update an existing assignment. Only allowed before due time or while in draft status.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param dto          updated data
     * @param userId       operator user id
     * @param roles        operator global roles
     * @return updated assignment
     */
    AssignmentVO updateAssignment(Long courseId, Long assignmentId, UpdateAssignmentRequestDTO dto,
                                  Long userId, List<String> roles);

    /**
     * Publish a draft assignment.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param userId       operator user id
     * @param roles        operator global roles
     */
    void publishAssignment(Long courseId, Long assignmentId, Long userId, List<String> roles);

    /**
     * Close a published assignment.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param userId       operator user id
     * @param roles        operator global roles
     */
    void closeAssignment(Long courseId, Long assignmentId, Long userId, List<String> roles);

    /**
     * Logically delete an assignment.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param userId       operator user id
     * @param roles        operator global roles
     */
    void deleteAssignment(Long courseId, Long assignmentId, Long userId, List<String> roles);

    /**
     * List assignments visible to the user within a course.
     *
     * @param courseId course id
     * @param userId   user id
     * @param roles    user global roles
     * @return assignment list
     */
    List<AssignmentVO> listAssignmentsByCourse(Long courseId, Long userId, List<String> roles);

    /**
     * Get assignment detail with files.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param userId       user id
     * @param roles        user global roles
     * @return assignment detail
     */
    AssignmentDetailVO getAssignmentDetail(Long courseId, Long assignmentId, Long userId, List<String> roles);

    /**
     * Add a placeholder file record to an assignment.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param dto          file metadata
     * @param userId       operator user id
     * @param roles        operator global roles
     * @return created file record
     */
    AssignmentFileVO addAssignmentFile(Long courseId, Long assignmentId, CreateAssignmentFileRequestDTO dto,
                                       Long userId, List<String> roles);

    /**
     * List placeholder files attached to an assignment.
     *
     * @param courseId     course id
     * @param assignmentId assignment id
     * @param userId       user id
     * @param roles        user global roles
     * @return file list
     */
    List<AssignmentFileVO> listAssignmentFiles(Long courseId, Long assignmentId, Long userId, List<String> roles);
}
