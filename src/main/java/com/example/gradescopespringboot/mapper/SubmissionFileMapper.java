package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.SubmissionFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SubmissionFileMapper {

    Optional<SubmissionFile> selectById(Long id);

    Optional<SubmissionFile> selectByFileUrl(@Param("fileUrl") String fileUrl);

    List<SubmissionFile> selectBySubmissionId(Long submissionId);

    int insert(SubmissionFile submissionFile);
}
