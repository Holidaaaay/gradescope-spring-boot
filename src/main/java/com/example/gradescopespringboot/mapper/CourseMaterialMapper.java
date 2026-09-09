package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.CourseMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseMaterialMapper {

    Optional<CourseMaterial> selectById(Long id);

    Optional<CourseMaterial> selectByFileUrl(@Param("fileUrl") String fileUrl);

    List<CourseMaterial> selectByCourseId(Long courseId);

    int insert(CourseMaterial courseMaterial);

    int deleteById(@Param("id") Long id);
}
