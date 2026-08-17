package com.example.gradescopespringboot.mapper;

import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.vo.admin.AdminUserListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(Long id);

    User selectByUsername(String username);

    int insert(User user);

    Long countAdminUsers(@Param("role") String role, @Param("status") Integer status);

    List<AdminUserListVO> selectAdminUserList(@Param("role") String role,
                                               @Param("status") Integer status,
                                               @Param("offset") int offset,
                                               @Param("pageSize") int pageSize);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

    Long countUsers();

    Long countCourses();

    Long countAssignments();

    Long countSubmissions();
}
