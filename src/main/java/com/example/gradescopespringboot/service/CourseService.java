package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.course.CreateCourseRequestDTO;
import com.example.gradescopespringboot.dto.course.EnrollMemberRequestDTO;
import com.example.gradescopespringboot.dto.course.UpdateCourseRequestDTO;
import com.example.gradescopespringboot.vo.course.CourseDetailVO;
import com.example.gradescopespringboot.vo.course.CourseVO;

import java.util.List;

public interface CourseService {

    /**
     * Create a new course. Creator is automatically enrolled as INSTRUCTOR.
     *
     * @param dto       course data
     * @param creatorId creator user id
     * @return created course
     */
    CourseVO createCourse(CreateCourseRequestDTO dto, Long creatorId);

    /**
     * Update an existing course.
     *
     * @param id        course id
     * @param dto       updated data
     * @param updaterId updater user id
     * @param roles     updater roles
     * @return updated course
     */
    CourseVO updateCourse(Long id, UpdateCourseRequestDTO dto, Long updaterId, List<String> roles);

    /**
     * Logically delete a course.
     *
     * @param id        course id
     * @param deleterId deleter user id
     * @param roles     deleter roles
     */
    void deleteCourse(Long id, Long deleterId, List<String> roles);

    /**
     * List courses visible to the user based on roles and memberships.
     *
     * @param userId user id
     * @param roles  user global roles
     * @return course list
     */
    List<CourseVO> listCourses(Long userId, List<String> roles);

    /**
     * Get course detail.
     *
     * @param id     course id
     * @param userId user id
     * @param roles  user global roles
     * @return course detail
     */
    CourseDetailVO getCourseDetail(Long id, Long userId, List<String> roles);

    /**
     * Enroll a member into a course.
     *
     * @param courseId   course id
     * @param dto        enrollment data
     * @param operatorId operator user id
     * @param roles      operator roles
     */
    void enrollMember(Long courseId, EnrollMemberRequestDTO dto, Long operatorId, List<String> roles);

    /**
     * Remove a member from a course.
     *
     * @param courseId  course id
     * @param userId    user id to remove
     * @param operatorId operator user id
     * @param roles     operator roles
     */
    void removeMember(Long courseId, Long userId, Long operatorId, List<String> roles);
}
