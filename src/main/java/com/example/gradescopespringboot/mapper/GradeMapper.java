package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.Grade;
import com.example.gradescopespringboot.vo.grade.GradeStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Mapper
public interface GradeMapper {

    Optional<Grade> selectById(Long id);

    Optional<Grade> selectBySubmissionId(@Param("submissionId") Long submissionId);

    int insert(Grade grade);

    int update(Grade grade);

    List<BigDecimal> selectFinalScoresByAssignmentId(@Param("assignmentId") Long assignmentId);

    GradeStatisticsVO selectStatistics(@Param("assignmentId") Long assignmentId);
}
