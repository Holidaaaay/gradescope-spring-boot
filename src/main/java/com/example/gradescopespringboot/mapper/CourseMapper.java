package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseMapper {

    Optional<Course> selectById(Long id);

    Optional<Course> selectByCodeAndSemester(@Param("courseCode") String courseCode,
                                              @Param("semester") String semester);

    int insert(Course course);

    int update(Course course);

    int deleteById(@Param("id") Long id, @Param("updatedBy") Long updatedBy);

    List<Course> selectByCreatedBy(Long createdBy);

    List<Course> selectByMemberUserId(Long userId);

    List<Course> selectAllActive();
}
