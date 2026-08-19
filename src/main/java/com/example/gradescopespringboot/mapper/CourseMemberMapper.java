package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.CourseMember;
import com.example.gradescopespringboot.vo.course.CourseMemberVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseMemberMapper {

    Optional<CourseMember> selectByCourseIdAndUserId(@Param("courseId") Long courseId,
                                                    @Param("userId") Long userId);

    List<CourseMember> selectByCourseId(Long courseId);

    List<CourseMemberVO> selectMembersByCourseId(Long courseId);

    int insert(CourseMember courseMember);

    int updateStatusByCourseIdAndUserId(@Param("courseId") Long courseId,
                                        @Param("userId") Long userId,
                                        @Param("status") Integer status);
}
