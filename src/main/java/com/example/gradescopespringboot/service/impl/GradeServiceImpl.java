package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.dto.grade.CreateGradeRequestDTO;
import com.example.gradescopespringboot.dto.grade.UpdateGradeRequestDTO;
import com.example.gradescopespringboot.entity.Assignment;
import com.example.gradescopespringboot.entity.Grade;
import com.example.gradescopespringboot.entity.Submission;
import com.example.gradescopespringboot.mapper.AssignmentMapper;
import com.example.gradescopespringboot.mapper.CourseMapper;
import com.example.gradescopespringboot.mapper.CourseMemberMapper;
import com.example.gradescopespringboot.mapper.GradeMapper;
import com.example.gradescopespringboot.mapper.SubmissionMapper;
import com.example.gradescopespringboot.service.GradeService;
import com.example.gradescopespringboot.vo.grade.GradeStatisticsVO;
import com.example.gradescopespringboot.vo.grade.GradeVO;
import com.example.gradescopespringboot.vo.grade.GradeWithSubmissionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GradeServiceImpl implements GradeService {

    /**
     * Submission status value meaning "graded"
     */
    private static final int SUBMISSION_STATUS_GRADED = 3;

    /**
     * Grade status value meaning "final / published"
     */
    private static final int GRADE_STATUS_FINAL = 1;

    private final CourseMapper courseMapper;
    private final CourseMemberMapper courseMemberMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;
    private final GradeMapper gradeMapper;

    public GradeServiceImpl(CourseMapper courseMapper,
                            CourseMemberMapper courseMemberMapper,
                            AssignmentMapper assignmentMapper,
                            SubmissionMapper submissionMapper,
                            GradeMapper gradeMapper) {
        this.courseMapper = courseMapper;
        this.courseMemberMapper = courseMemberMapper;
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.gradeMapper = gradeMapper;
    }

    @Override
    @Transactional
    public GradeVO createGrade(Long courseId, Long assignmentId, Long submissionId,
                               CreateGradeRequestDTO dto, Long scorerId, List<String> roles) {
        checkGrader(courseId, scorerId, roles);
        Assignment assignment = checkAssignment(courseId, assignmentId);
        Submission submission = checkSubmission(assignmentId, submissionId);

        if (Integer.valueOf(0).equals(submission.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Cannot grade a draft submission");
        }
        if (gradeMapper.selectBySubmissionId(submissionId).isPresent()) {
            throw new BusinessException(ResultCode.CONFLICT, "Submission already graded");
        }
        validateScore(dto.getScore(), assignment.getTotalScore());

        int status = dto.getStatus() == null ? GRADE_STATUS_FINAL : dto.getStatus();
        Grade grade = new Grade();
        grade.setSubmissionId(submissionId);
        grade.setScorerId(scorerId);
        grade.setScore(dto.getScore());
        grade.setComment(dto.getComment());
        grade.setGradedAt(LocalDateTime.now());
        grade.setStatus(status);

        gradeMapper.insert(grade);
        if (status == GRADE_STATUS_FINAL) {
            submissionMapper.updateStatusById(submissionId, SUBMISSION_STATUS_GRADED);
        }
        return toGradeVO(grade);
    }

    @Override
    @Transactional
    public GradeVO updateGrade(Long courseId, Long assignmentId, Long submissionId,
                               UpdateGradeRequestDTO dto, Long scorerId, List<String> roles) {
        checkGrader(courseId, scorerId, roles);
        Assignment assignment = checkAssignment(courseId, assignmentId);
        checkSubmission(assignmentId, submissionId);

        Grade grade = gradeMapper.selectBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found for submission: " + submissionId));

        boolean regrade = Boolean.TRUE.equals(dto.getRegrade());
        if (grade.getStatus() == GRADE_STATUS_FINAL && !regrade) {
            throw new BusinessException(ResultCode.CONFLICT,
                    "Grade is final; set regrade=true to modify it");
        }
        validateScore(dto.getScore(), assignment.getTotalScore());

        grade.setScore(dto.getScore());
        grade.setComment(dto.getComment());
        grade.setGradedAt(LocalDateTime.now());
        if (dto.getStatus() != null) {
            grade.setStatus(dto.getStatus());
        }

        gradeMapper.update(grade);
        if (grade.getStatus() == GRADE_STATUS_FINAL) {
            submissionMapper.updateStatusById(submissionId, SUBMISSION_STATUS_GRADED);
        }
        return toGradeVO(grade);
    }

    @Override
    public GradeWithSubmissionVO getGradeBySubmission(Long courseId, Long assignmentId, Long submissionId,
                                                      Long userId, List<String> roles) {
        checkCourseMember(courseId, userId, roles);
        checkAssignment(courseId, assignmentId);
        Submission submission = checkSubmission(assignmentId, submissionId);

        Grade grade = gradeMapper.selectBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found for submission: " + submissionId));

        boolean staff = roles.contains("ADMIN") || isInstructorOrTa(courseId, userId);
        if (!staff) {
            if (!submission.getStudentId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "You can only view your own grades");
            }
            if (grade.getStatus() != GRADE_STATUS_FINAL) {
                throw new ResourceNotFoundException("Grade not found for submission: " + submissionId);
            }
        }
        return toGradeWithSubmissionVO(grade, submission);
    }

    @Override
    public GradeStatisticsVO getGradeStatistics(Long courseId, Long assignmentId,
                                                Long userId, List<String> roles) {
        checkGrader(courseId, userId, roles);
        checkAssignment(courseId, assignmentId);

        GradeStatisticsVO statistics = gradeMapper.selectStatistics(assignmentId);
        statistics.setMedianScore(
                computeMedian(gradeMapper.selectFinalScoresByAssignmentId(assignmentId)));
        return statistics;
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

    private void checkGrader(Long courseId, Long userId, List<String> roles) {
        if (roles.contains("ADMIN")) {
            return;
        }
        if (!isInstructorOrTa(courseId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only instructors or TAs can grade submissions");
        }
    }

    private boolean isInstructorOrTa(Long courseId, Long userId) {
        return courseMemberMapper.selectByCourseIdAndUserId(courseId, userId)
                .map(m -> "INSTRUCTOR".equals(m.getCourseRole()) || "TA".equals(m.getCourseRole()))
                .orElse(false);
    }

    private Assignment checkAssignment(Long courseId, Long assignmentId) {
        return assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));
    }

    private Submission checkSubmission(Long assignmentId, Long submissionId) {
        return submissionMapper.selectByIdAndAssignmentId(submissionId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found: " + submissionId));
    }

    private void validateScore(BigDecimal score, BigDecimal totalScore) {
        if (score.compareTo(totalScore) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "Score exceeds assignment total score: " + totalScore);
        }
    }

    private BigDecimal computeMedian(List<BigDecimal> scores) {
        if (scores.isEmpty()) {
            return null;
        }
        int size = scores.size();
        if (size % 2 == 1) {
            return scores.get(size / 2);
        }
        return scores.get(size / 2 - 1).add(scores.get(size / 2))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private GradeVO toGradeVO(Grade grade) {
        GradeVO vo = new GradeVO();
        vo.setId(grade.getId());
        vo.setSubmissionId(grade.getSubmissionId());
        vo.setScorerId(grade.getScorerId());
        vo.setScore(grade.getScore());
        vo.setComment(grade.getComment());
        vo.setGradedAt(grade.getGradedAt());
        vo.setStatus(grade.getStatus());
        vo.setCreatedAt(grade.getCreatedAt());
        vo.setUpdatedAt(grade.getUpdatedAt());
        return vo;
    }

    private GradeWithSubmissionVO toGradeWithSubmissionVO(Grade grade, Submission submission) {
        GradeWithSubmissionVO vo = new GradeWithSubmissionVO();
        vo.setId(grade.getId());
        vo.setSubmissionId(grade.getSubmissionId());
        vo.setScorerId(grade.getScorerId());
        vo.setScore(grade.getScore());
        vo.setComment(grade.getComment());
        vo.setGradedAt(grade.getGradedAt());
        vo.setStatus(grade.getStatus());
        vo.setStudentId(submission.getStudentId());
        vo.setSubmissionNo(submission.getSubmissionNo());
        vo.setContentText(submission.getContentText());
        vo.setSubmittedAt(submission.getSubmittedAt());
        vo.setIsLate(Integer.valueOf(1).equals(submission.getIsLate()));
        return vo;
    }
}
