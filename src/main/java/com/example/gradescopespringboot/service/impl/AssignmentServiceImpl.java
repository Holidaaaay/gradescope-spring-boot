package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.dto.assignment.CreateAssignmentFileRequestDTO;
import com.example.gradescopespringboot.dto.assignment.CreateAssignmentRequestDTO;
import com.example.gradescopespringboot.dto.assignment.UpdateAssignmentRequestDTO;
import com.example.gradescopespringboot.entity.Assignment;
import com.example.gradescopespringboot.entity.AssignmentFile;
import com.example.gradescopespringboot.entity.Course;
import com.example.gradescopespringboot.entity.CourseMember;
import com.example.gradescopespringboot.mapper.AssignmentFileMapper;
import com.example.gradescopespringboot.mapper.AssignmentMapper;
import com.example.gradescopespringboot.mapper.CourseMapper;
import com.example.gradescopespringboot.mapper.CourseMemberMapper;
import com.example.gradescopespringboot.service.AssignmentService;
import com.example.gradescopespringboot.vo.assignment.AssignmentDetailVO;
import com.example.gradescopespringboot.vo.assignment.AssignmentFileVO;
import com.example.gradescopespringboot.vo.assignment.AssignmentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final CourseMapper courseMapper;
    private final CourseMemberMapper courseMemberMapper;
    private final AssignmentMapper assignmentMapper;
    private final AssignmentFileMapper assignmentFileMapper;

    public AssignmentServiceImpl(CourseMapper courseMapper,
                                 CourseMemberMapper courseMemberMapper,
                                 AssignmentMapper assignmentMapper,
                                 AssignmentFileMapper assignmentFileMapper) {
        this.courseMapper = courseMapper;
        this.courseMemberMapper = courseMemberMapper;
        this.assignmentMapper = assignmentMapper;
        this.assignmentFileMapper = assignmentFileMapper;
    }

    @Override
    @Transactional
    public AssignmentVO createAssignment(Long courseId, CreateAssignmentRequestDTO dto,
                                         Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseStaff(course, userId, roles, "create assignment");

        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setTotalScore(dto.getTotalScore());
        assignment.setDueTime(dto.getDueTime());
        assignment.setAllowLateSubmission(booleanToTinyInt(dto.getAllowLateSubmission()));
        assignment.setMaxSubmissionTimes(dto.getMaxSubmissionTimes());
        assignment.setStatus(0);
        assignment.setCreatedBy(userId);
        assignment.setUpdatedBy(userId);
        assignment.setIsDeleted(0);

        assignmentMapper.insert(assignment);
        return toAssignmentVO(assignment);
    }

    @Override
    @Transactional
    public AssignmentVO updateAssignment(Long courseId, Long assignmentId, UpdateAssignmentRequestDTO dto,
                                         Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseStaff(course, userId, roles, "update assignment");

        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() == 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Cannot update a closed assignment");
        }
        if (assignment.getStatus() == 1 && LocalDateTime.now().isAfter(assignment.getDueTime())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Cannot update assignment after due time");
        }

        if (dto.getTitle() != null) {
            assignment.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            assignment.setDescription(dto.getDescription());
        }
        if (dto.getTotalScore() != null) {
            assignment.setTotalScore(dto.getTotalScore());
        }
        if (dto.getDueTime() != null) {
            assignment.setDueTime(dto.getDueTime());
        }
        if (dto.getAllowLateSubmission() != null) {
            assignment.setAllowLateSubmission(booleanToTinyInt(dto.getAllowLateSubmission()));
        }
        if (dto.getMaxSubmissionTimes() != null) {
            assignment.setMaxSubmissionTimes(dto.getMaxSubmissionTimes());
        }
        assignment.setUpdatedBy(userId);

        assignmentMapper.update(assignment);
        return toAssignmentVO(assignment);
    }

    @Override
    @Transactional
    public void publishAssignment(Long courseId, Long assignmentId, Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseStaff(course, userId, roles, "publish assignment");

        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() != 0) {
            throw new BusinessException(ResultCode.CONFLICT, "Assignment is not in draft status");
        }

        assignment.setStatus(1);
        assignment.setUpdatedBy(userId);
        assignmentMapper.update(assignment);
    }

    @Override
    @Transactional
    public void closeAssignment(Long courseId, Long assignmentId, Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseStaff(course, userId, roles, "close assignment");

        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() == 2) {
            throw new BusinessException(ResultCode.CONFLICT, "Assignment is already closed");
        }

        assignment.setStatus(2);
        assignment.setUpdatedBy(userId);
        assignmentMapper.update(assignment);
    }

    @Override
    @Transactional
    public void deleteAssignment(Long courseId, Long assignmentId, Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseStaff(course, userId, roles, "delete assignment");

        if (assignmentMapper.selectByIdAndCourseId(assignmentId, courseId).isEmpty()) {
            throw new ResourceNotFoundException("Assignment not found: " + assignmentId);
        }

        assignmentMapper.deleteById(assignmentId, userId);
    }

    @Override
    public List<AssignmentVO> listAssignmentsByCourse(Long courseId, Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseMember(course, userId, roles);

        List<Assignment> assignments;
        if (roles.contains("ADMIN") || isInstructorOrTa(courseId, userId)) {
            assignments = assignmentMapper.selectByCourseId(courseId);
        } else {
            assignments = assignmentMapper.selectPublishedByCourseId(courseId);
        }
        return assignments.stream().map(this::toAssignmentVO).toList();
    }

    @Override
    public AssignmentDetailVO getAssignmentDetail(Long courseId, Long assignmentId, Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseMember(course, userId, roles);

        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        boolean canSeeAll = roles.contains("ADMIN") || isInstructorOrTa(courseId, userId);
        if (!canSeeAll && assignment.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Assignment is not published");
        }

        AssignmentDetailVO detail = toAssignmentDetailVO(assignment);
        List<AssignmentFileVO> files = assignmentFileMapper.selectByAssignmentId(assignmentId)
                .stream()
                .map(this::toAssignmentFileVO)
                .toList();
        detail.setFiles(files);
        return detail;
    }

    @Override
    @Transactional
    public AssignmentFileVO addAssignmentFile(Long courseId, Long assignmentId, CreateAssignmentFileRequestDTO dto,
                                              Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseStaff(course, userId, roles, "add assignment file");

        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        if (assignment.getStatus() == 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Cannot add files to a closed assignment");
        }

        AssignmentFile file = new AssignmentFile();
        file.setAssignmentId(assignmentId);
        file.setFileName(dto.getFileName());
        file.setFileUrl(dto.getFileUrl());
        file.setFileSize(dto.getFileSize());
        file.setFileType(dto.getFileType());
        file.setUploadedBy(userId);

        assignmentFileMapper.insert(file);
        return toAssignmentFileVO(file);
    }

    @Override
    public List<AssignmentFileVO> listAssignmentFiles(Long courseId, Long assignmentId, Long userId, List<String> roles) {
        Course course = checkCourseExists(courseId);
        checkCourseMember(course, userId, roles);

        Assignment assignment = assignmentMapper.selectByIdAndCourseId(assignmentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found: " + assignmentId));

        boolean canSeeAll = roles.contains("ADMIN") || isInstructorOrTa(courseId, userId);
        if (!canSeeAll && assignment.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Assignment is not published");
        }

        return assignmentFileMapper.selectByAssignmentId(assignmentId)
                .stream()
                .map(this::toAssignmentFileVO)
                .toList();
    }

    private Course checkCourseExists(Long courseId) {
        return courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    private void checkCourseMember(Course course, Long userId, List<String> roles) {
        if (roles.contains("ADMIN")) {
            return;
        }
        courseMemberMapper.selectByCourseIdAndUserId(course.getId(), userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course"));
    }

    private void checkCourseStaff(Course course, Long userId, List<String> roles, String action) {
        if (roles.contains("ADMIN")) {
            return;
        }
        CourseMember member = courseMemberMapper.selectByCourseIdAndUserId(course.getId(), userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course"));
        if (!"INSTRUCTOR".equals(member.getCourseRole()) && !"TA".equals(member.getCourseRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "Only course instructor or TA can " + action);
        }
    }

    private boolean isInstructorOrTa(Long courseId, Long userId) {
        return courseMemberMapper.selectByCourseIdAndUserId(courseId, userId)
                .map(m -> "INSTRUCTOR".equals(m.getCourseRole()) || "TA".equals(m.getCourseRole()))
                .orElse(false);
    }

    private Integer booleanToTinyInt(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    private Boolean tinyIntToBoolean(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    private AssignmentVO toAssignmentVO(Assignment assignment) {
        AssignmentVO vo = new AssignmentVO();
        vo.setId(assignment.getId());
        vo.setCourseId(assignment.getCourseId());
        vo.setTitle(assignment.getTitle());
        vo.setDescription(assignment.getDescription());
        vo.setTotalScore(assignment.getTotalScore());
        vo.setDueTime(assignment.getDueTime());
        vo.setAllowLateSubmission(tinyIntToBoolean(assignment.getAllowLateSubmission()));
        vo.setMaxSubmissionTimes(assignment.getMaxSubmissionTimes());
        vo.setStatus(assignment.getStatus());
        vo.setCreatedBy(assignment.getCreatedBy());
        vo.setUpdatedBy(assignment.getUpdatedBy());
        vo.setCreatedAt(assignment.getCreatedAt());
        vo.setUpdatedAt(assignment.getUpdatedAt());
        return vo;
    }

    private AssignmentDetailVO toAssignmentDetailVO(Assignment assignment) {
        AssignmentDetailVO vo = new AssignmentDetailVO();
        vo.setId(assignment.getId());
        vo.setCourseId(assignment.getCourseId());
        vo.setTitle(assignment.getTitle());
        vo.setDescription(assignment.getDescription());
        vo.setTotalScore(assignment.getTotalScore());
        vo.setDueTime(assignment.getDueTime());
        vo.setAllowLateSubmission(tinyIntToBoolean(assignment.getAllowLateSubmission()));
        vo.setMaxSubmissionTimes(assignment.getMaxSubmissionTimes());
        vo.setStatus(assignment.getStatus());
        vo.setCreatedBy(assignment.getCreatedBy());
        vo.setUpdatedBy(assignment.getUpdatedBy());
        vo.setCreatedAt(assignment.getCreatedAt());
        vo.setUpdatedAt(assignment.getUpdatedAt());
        return vo;
    }

    private AssignmentFileVO toAssignmentFileVO(AssignmentFile file) {
        AssignmentFileVO vo = new AssignmentFileVO();
        vo.setId(file.getId());
        vo.setAssignmentId(file.getAssignmentId());
        vo.setFileName(file.getFileName());
        vo.setFileUrl(file.getFileUrl());
        vo.setFileSize(file.getFileSize());
        vo.setFileType(file.getFileType());
        vo.setUploadedBy(file.getUploadedBy());
        vo.setCreatedAt(file.getCreatedAt());
        return vo;
    }
}
