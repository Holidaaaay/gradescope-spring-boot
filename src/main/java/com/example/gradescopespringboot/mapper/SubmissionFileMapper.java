package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.SubmissionFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SubmissionFileMapper {

    Optional<SubmissionFile> selectById(Long id);

    List<SubmissionFile> selectBySubmissionId(Long submissionId);

    int insert(SubmissionFile submissionFile);
}
