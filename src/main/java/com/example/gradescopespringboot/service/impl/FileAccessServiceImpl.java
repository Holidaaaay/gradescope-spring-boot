package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.entity.AssignmentFile;
import com.example.gradescopespringboot.entity.CourseMaterial;
import com.example.gradescopespringboot.entity.SubmissionFile;
import com.example.gradescopespringboot.mapper.AssignmentFileMapper;
import com.example.gradescopespringboot.mapper.AssignmentMapper;
import com.example.gradescopespringboot.mapper.CourseMaterialMapper;
import com.example.gradescopespringboot.mapper.CourseMemberMapper;
import com.example.gradescopespringboot.mapper.SubmissionFileMapper;
import com.example.gradescopespringboot.mapper.SubmissionMapper;
import com.example.gradescopespringboot.service.FileAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileAccessServiceImpl implements FileAccessService {

    private static final Logger log = LoggerFactory.getLogger(FileAccessServiceImpl.class);

    private final AssignmentFileMapper assignmentFileMapper;
    private final SubmissionFileMapper submissionFileMapper;
    private final CourseMaterialMapper courseMaterialMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;
    private final CourseMemberMapper courseMemberMapper;

    public FileAccessServiceImpl(AssignmentFileMapper assignmentFileMapper,
                                 SubmissionFileMapper submissionFileMapper,
                                 CourseMaterialMapper courseMaterialMapper,
                                 AssignmentMapper assignmentMapper,
                                 SubmissionMapper submissionMapper,
                                 CourseMemberMapper courseMemberMapper) {
        this.assignmentFileMapper = assignmentFileMapper;
        this.submissionFileMapper = submissionFileMapper;
        this.courseMaterialMapper = courseMaterialMapper;
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.courseMemberMapper = courseMemberMapper;
    }

    @Override
    public Long findCourseIdByFileUrl(String fileUrl) {
        log.debug("Resolving course for fileUrl={}", fileUrl);
        java.util.Optional<AssignmentFile> assignmentFile = assignmentFileMapper.selectByFileUrl(fileUrl);
        if (assignmentFile.isPresent()) {
            log.debug("Found assignment file id={}", assignmentFile.get().getId());
            return assignmentMapper.selectById(assignmentFile.get().getAssignmentId())
                    .map(a -> a.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found for file"));
        }

        java.util.Optional<SubmissionFile> submissionFile = submissionFileMapper.selectByFileUrl(fileUrl);
        if (submissionFile.isPresent()) {
            log.debug("Found submission file id={}", submissionFile.get().getId());
            return submissionMapper.selectById(submissionFile.get().getSubmissionId())
                    .flatMap(s -> assignmentMapper.selectById(s.getAssignmentId()))
                    .map(a -> a.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found for file"));
        }

        java.util.Optional<CourseMaterial> material = courseMaterialMapper.selectByFileUrl(fileUrl);
        if (material.isPresent()) {
            log.debug("Found course material id={}", material.get().getId());
            return material.get().getCourseId();
        }

        log.warn("No file record matched fileUrl={}", fileUrl);
        throw new ResourceNotFoundException("File record not found");
    }

    @Override
    public void checkCourseAccess(Long courseId, Long userId, List<String> roles) {
        if (roles.contains("ADMIN")) {
            return;
        }
        courseMemberMapper.selectByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course"));
    }
}
