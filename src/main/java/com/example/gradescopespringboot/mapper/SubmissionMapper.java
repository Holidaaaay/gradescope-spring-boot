package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SubmissionMapper {

    Optional<Submission> selectById(Long id);

    Optional<Submission> selectByIdAndAssignmentId(@Param("id") Long id,
                                                    @Param("assignmentId") Long assignmentId);

    List<Submission> selectByAssignmentIdAndStudentId(@Param("assignmentId") Long assignmentId,
                                                     @Param("studentId") Long studentId);

    List<Submission> selectByAssignmentId(Long assignmentId);

    int countSubmittedByAssignmentAndStudent(@Param("assignmentId") Long assignmentId,
                                             @Param("studentId") Long studentId);

    Integer selectMaxSubmissionNo(@Param("assignmentId") Long assignmentId,
                                  @Param("studentId") Long studentId);

    Optional<Submission> selectDraftByAssignmentAndStudent(@Param("assignmentId") Long assignmentId,
                                                           @Param("studentId") Long studentId);

    int insert(Submission submission);

    int update(Submission submission);

    int updateDraft(@Param("assignmentId") Long assignmentId,
                    @Param("studentId") Long studentId,
                    @Param("contentText") String contentText);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);
}
