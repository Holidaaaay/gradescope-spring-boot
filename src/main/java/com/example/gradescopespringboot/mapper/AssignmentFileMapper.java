package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.AssignmentFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AssignmentFileMapper {

    Optional<AssignmentFile> selectById(Long id);

    Optional<AssignmentFile> selectByFileUrl(@Param("fileUrl") String fileUrl);

    List<AssignmentFile> selectByAssignmentId(Long assignmentId);

    int insert(AssignmentFile assignmentFile);

    int deleteById(Long id);
}
