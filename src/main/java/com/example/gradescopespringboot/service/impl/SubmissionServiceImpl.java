package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.dto.submission.CreateSubmissionRequestDTO;
import com.example.gradescopespringboot.entity.Assignment;
import com.example.gradescopespringboot.entity.Submission;
import com.example.gradescopespringboot.mapper.AssignmentMapper;
import com.example.gradescopespringboot.mapper.CourseMapper;
import com.example.gradescopespringboot.mapper.CourseMemberMapper;
import com.example.gradescopespringboot.mapper.SubmissionFileMapper;
import com.example.gradescopespringboot.mapper.SubmissionMapper;
import com.example.gradescopespringboot.service.SubmissionService;
import com.example.gradescopespringboot.vo.submission.SubmissionDetailVO;
import com.example.gradescopespringboot.vo.submission.SubmissionFileVO;
import com.example.gradescopespringboot.vo.submission.SubmissionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final CourseMapper courseMapper;
    private final CourseMemberMapper courseMemberMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionFileMapper submissionFileMapper;

    public SubmissionServiceImpl(CourseMapper courseMapper,
                                 CourseMemberMapper courseMemberMapper,
                                 AssignmentMapper assignmentMapper,
                                 SubmissionMapper submissionMapper,
                                 SubmissionFileMapper submissionFileMapper) {
        this.courseMapper = courseMapper;
        this.courseMemberMapper = courseMemberMapper;
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.submissionFileMapper = submissionFileMapper;
    }

    @Override
    @Transactional
    public SubmissionVO createSubmission(Long courseId, Long assignmentId, CreateSubmissionRequestDTO dto,
                                         Long studentId, List<String> roles) {
        checkStudentMember(courseId, studentId, roles);
        Assignment assignment = checkPublishedAssignment(courseId, assignmentId);

        LocalDateTime now = LocalDateTime.now();
        boolean late = now.isAfter(assignment.getDueTime());
        if (late && !isLateAllowed(assignment)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Late submission is not allowed for this assignment");
        }

        int submittedCount = submissionMapper.countSubmittedByAssignmentAndStudent(assignmentId, studentId);
        if (submittedCount >= assignment.getMaxSubmissionTimes()) {
            throw new BusinessException(ResultCode.CONFLICT,
                    "Maximum submission times reached: " + assignment.getMaxSubmissionTimes());
        }

        Integer nextSubmissionNo = Optional.ofNullable(
                submissionMapper.selectMaxSubmissionNo(assignmentId, studentId)
        ).map(max -> max + 1).orElse(1);

        Submission submission = new Submission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setSubmissionNo(nextSubmissionNo);
        submission.setContentText(dto.getContentText());
        submission.setSubmittedAt(now);
        submission.setIsLate(late ? 1 : 0);
        submission.setStatus(1);

        submissionMapper.insert(submission);
        return toSubmissionVO(submission);
    }

    @Override
    @Transactional
    public SubmissionVO saveDraft(Long courseId, Long assignmentId, CreateSubmissionRequestDTO dto,
                                  Long studentId, List<String> roles) {
        checkStudentMember(courseId, studentId, roles);
        checkPublishedAssignment(courseId, assignmentId);

        Optional<Submission> existingDraft = submissionMapper.selectDraftByAssignmentAndStudent(assignmentId, studentId);
        if (existingDraft.isPresent()) {
            Submission draft = existingDraft.get();
            submissionMapper.updateDraft(assignmentId, studentId, dto.getContentText());
            draft.setContentText(dto.getContentText());
            return toSubmissionVO(draft);
        }

        Submission draft = new Submission();
        draft.setAssignmentId(assignmentId);
        draft.setStudentId(studentId);
        draft.setSubmissionNo(1);
        draft.setContentText(dto.getContentText());
        draft.setSubmittedAt(LocalDateTime.now());
        draft.setIsLate(0);
        draft.setStatus(0);

        submissionMapper.insert(draft);
        return toSubmissionVO(draft);
    }

    @Override
    public List<SubmissionVO> listSubmissions(Long courseId, Long assignmentId, Long userId, List<String> roles) {
        checkCourseMember(courseId, userId, roles);
        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        boolean canSeeAll = roles.contains("ADMIN") || isInstructorOrTa(courseId, userId);
        List<Submission> submissions;
        if (canSeeAll) {
            submissions = submissionMapper.selectByAssignmentId(assignmentId);
        } else {
            submissions = submissionMapper.selectByAssignmentIdAndStudentId(assignmentId, userId);
        }
        return submissions.stream().map(this::toSubmissionVO).toList();
    }

    @Override
    public SubmissionDetailVO getSubmissionDetail(Long courseId, Long assignmentId, Long submissionId,
                                                  Long userId, List<String> roles) {
        checkCourseMember(courseId, userId, roles);
        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        Submission submission = submissionMapper.selectByIdAndAssignmentId(submissionId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));

        boolean canSeeAll = roles.contains("ADMIN") || isInstructorOrTa(courseId, userId);
        if (!canSeeAll && !submission.getStudentId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You can only view your own submissions");
        }

        SubmissionDetailVO detail = toSubmissionDetailVO(submission);
        List<SubmissionFileVO> files = submissionFileMapper.selectBySubmissionId(submissionId)
                .stream()
                .map(this::toSubmissionFileVO)
                .toList();
        detail.setFiles(files);
        return detail;
    }

    private void checkCourseMember(Long courseId, Long userId, List<String> roles) {
        if (roles.contains("ADMIN")) {
            return;
        }
        courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        courseMemberMapper.selectByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course"));
    }

    private void checkStudentMember(Long courseId, Long studentId, List<String> roles) {
        if (!roles.contains("STUDENT")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only students can submit assignments");
        }
        checkCourseMember(courseId, studentId, roles);
    }

    private Assignment checkPublishedAssignment(Long courseId, Long assignmentId) {
        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));
        if (assignment.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Assignment is not published");
        }
        return assignment;
    }

    private boolean isInstructorOrTa(Long courseId, Long userId) {
        return courseMemberMapper.selectByCourseIdAndUserId(courseId, userId)
                .map(m -> "INSTRUCTOR".equals(m.getCourseRole()) || "TA".equals(m.getCourseRole()))
                .orElse(false);
    }

    private boolean isLateAllowed(Assignment assignment) {
        return Integer.valueOf(1).equals(assignment.getAllowLateSubmission());
    }

    private SubmissionVO toSubmissionVO(Submission submission) {
        SubmissionVO vo = new SubmissionVO();
        vo.setId(submission.getId());
        vo.setAssignmentId(submission.getAssignmentId());
        vo.setStudentId(submission.getStudentId());
        vo.setSubmissionNo(submission.getSubmissionNo());
        vo.setContentText(submission.getContentText());
        vo.setSubmittedAt(submission.getSubmittedAt());
        vo.setIsLate(Integer.valueOf(1).equals(submission.getIsLate()));
        vo.setStatus(submission.getStatus());
        vo.setCreatedAt(submission.getCreatedAt());
        vo.setUpdatedAt(submission.getUpdatedAt());
        return vo;
    }

    private SubmissionDetailVO toSubmissionDetailVO(Submission submission) {
        SubmissionDetailVO vo = new SubmissionDetailVO();
        vo.setId(submission.getId());
        vo.setAssignmentId(submission.getAssignmentId());
        vo.setStudentId(submission.getStudentId());
        vo.setSubmissionNo(submission.getSubmissionNo());
        vo.setContentText(submission.getContentText());
        vo.setSubmittedAt(submission.getSubmittedAt());
        vo.setIsLate(Integer.valueOf(1).equals(submission.getIsLate()));
        vo.setStatus(submission.getStatus());
        vo.setCreatedAt(submission.getCreatedAt());
        vo.setUpdatedAt(submission.getUpdatedAt());
        return vo;
    }

    private SubmissionFileVO toSubmissionFileVO(com.example.gradescopespringboot.entity.SubmissionFile file) {
        SubmissionFileVO vo = new SubmissionFileVO();
        vo.setId(file.getId());
        vo.setSubmissionId(file.getSubmissionId());
        vo.setFileName(file.getFileName());
        vo.setFileUrl(file.getFileUrl());
        vo.setFileSize(file.getFileSize());
        vo.setFileType(file.getFileType());
        vo.setCreatedAt(file.getCreatedAt());
        return vo;
    }
}
