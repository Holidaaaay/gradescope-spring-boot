package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.Assignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AssignmentMapper {

    Optional<Assignment> selectById(Long id);

    Optional<Assignment> selectByIdAndCourseId(@Param("id") Long id,
                                                @Param("courseId") Long courseId);

    List<Assignment> selectByCourseId(Long courseId);

    List<Assignment> selectPublishedByCourseId(Long courseId);

    int insert(Assignment assignment);

    int update(Assignment assignment);

    int deleteById(@Param("id") Long id, @Param("updatedBy") Long updatedBy);
}
