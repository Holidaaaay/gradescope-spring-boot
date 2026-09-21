package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.grade.CreateGradeRequestDTO;
import com.example.gradescopespringboot.dto.grade.UpdateGradeRequestDTO;
import com.example.gradescopespringboot.vo.grade.GradeStatisticsVO;
import com.example.gradescopespringboot.vo.grade.GradeVO;
import com.example.gradescopespringboot.vo.grade.GradeWithSubmissionVO;

import java.util.List;

public interface GradeService {

    GradeVO createGrade(Long courseId, Long assignmentId, Long submissionId,
                        CreateGradeRequestDTO dto, Long scorerId, List<String> roles);

    GradeVO updateGrade(Long courseId, Long assignmentId, Long submissionId,
                        UpdateGradeRequestDTO dto, Long scorerId, List<String> roles);

    GradeWithSubmissionVO getGradeBySubmission(Long courseId, Long assignmentId, Long submissionId,
                                               Long userId, List<String> roles);

    GradeStatisticsVO getGradeStatistics(Long courseId, Long assignmentId,
                                         Long userId, List<String> roles);
}
