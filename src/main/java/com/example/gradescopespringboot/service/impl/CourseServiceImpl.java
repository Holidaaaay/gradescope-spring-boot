package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.dto.course.CreateCourseRequestDTO;
import com.example.gradescopespringboot.dto.course.EnrollMemberRequestDTO;
import com.example.gradescopespringboot.dto.course.UpdateCourseRequestDTO;
import com.example.gradescopespringboot.entity.Course;
import com.example.gradescopespringboot.entity.CourseMember;
import com.example.gradescopespringboot.entity.User;
import com.example.gradescopespringboot.mapper.CourseMapper;
import com.example.gradescopespringboot.mapper.CourseMemberMapper;
import com.example.gradescopespringboot.service.CourseService;
import com.example.gradescopespringboot.service.UserService;
import com.example.gradescopespringboot.vo.course.CourseDetailVO;
import com.example.gradescopespringboot.vo.course.CourseMemberVO;
import com.example.gradescopespringboot.vo.course.CourseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseMemberMapper courseMemberMapper;
    private final UserService userService;

    public CourseServiceImpl(CourseMapper courseMapper,
                             CourseMemberMapper courseMemberMapper,
                             UserService userService) {
        this.courseMapper = courseMapper;
        this.courseMemberMapper = courseMemberMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public CourseVO createCourse(CreateCourseRequestDTO dto, Long creatorId) {
        if (courseMapper.selectByCodeAndSemester(dto.getCourseCode(), dto.getSemester()).isPresent()) {
            throw new BusinessException(ResultCode.CONFLICT, "Course code and semester already exist");
        }

        Course course = new Course();
        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setSemester(dto.getSemester());
        course.setDescription(dto.getDescription());
        course.setLocation(dto.getLocation());
        course.setScheduleInfo(dto.getScheduleInfo());
        course.setStatus(1);
        course.setCreatedBy(creatorId);
        course.setUpdatedBy(creatorId);
        course.setIsDeleted(0);

        courseMapper.insert(course);

        CourseMember creatorMember = new CourseMember();
        creatorMember.setCourseId(course.getId());
        creatorMember.setUserId(creatorId);
        creatorMember.setCourseRole("INSTRUCTOR");
        creatorMember.setStatus(1);
        courseMemberMapper.insert(creatorMember);

        return toCourseVO(course);
    }

    @Override
    @Transactional
    public CourseVO updateCourse(Long id, UpdateCourseRequestDTO dto, Long updaterId, List<String> roles) {
        Course course = courseMapper.selectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));

        if (!course.getCreatedBy().equals(updaterId) && !roles.contains("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only creator or admin can update this course");
        }

        boolean codeOrSemesterChanged = !Objects.equals(course.getCourseCode(), dto.getCourseCode())
                || !Objects.equals(course.getSemester(), dto.getSemester());
        if (codeOrSemesterChanged) {
            courseMapper.selectByCodeAndSemester(dto.getCourseCode(), dto.getSemester())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BusinessException(ResultCode.CONFLICT, "Course code and semester already exist");
                        }
                    });
        }

        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setSemester(dto.getSemester());
        course.setDescription(dto.getDescription());
        course.setLocation(dto.getLocation());
        course.setScheduleInfo(dto.getScheduleInfo());
        if (dto.getStatus() != null) {
            course.setStatus(dto.getStatus());
        }
        course.setUpdatedBy(updaterId);

        courseMapper.update(course);
        return toCourseVO(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id, Long deleterId, List<String> roles) {
        Course course = courseMapper.selectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));

        if (!course.getCreatedBy().equals(deleterId) && !roles.contains("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only creator or admin can delete this course");
        }

        courseMapper.deleteById(id, deleterId);
    }

    @Override
    public List<CourseVO> listCourses(Long userId, List<String> roles) {
        List<Course> courses;
        if (roles.contains("ADMIN")) {
            courses = courseMapper.selectAllActive();
        } else if (roles.contains("INSTRUCTOR") || roles.contains("TA")) {
            List<Course> created = courseMapper.selectByCreatedBy(userId);
            List<Course> member = courseMapper.selectByMemberUserId(userId);
            Map<Long, Course> merged = new LinkedHashMap<>();
            created.forEach(c -> merged.put(c.getId(), c));
            member.forEach(c -> merged.put(c.getId(), c));
            courses = new ArrayList<>(merged.values());
        } else {
            courses = courseMapper.selectByMemberUserId(userId);
        }
        return courses.stream().map(this::toCourseVO).toList();
    }

    @Override
    public CourseDetailVO getCourseDetail(Long id, Long userId, List<String> roles) {
        Course course = courseMapper.selectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));

        boolean isAdmin = roles.contains("ADMIN");
        boolean isInstructorOrTa = roles.contains("INSTRUCTOR") || roles.contains("TA");
        boolean isMember = courseMemberMapper.selectByCourseIdAndUserId(id, userId).isPresent();

        if (!isAdmin && !isMember) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course");
        }

        CourseDetailVO detail = toCourseDetailVO(course);
        List<CourseMemberVO> members = courseMemberMapper.selectMembersByCourseId(id);

        if (!isAdmin && !isInstructorOrTa) {
            members = members.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .toList();
        }
        detail.setMembers(members);

        return detail;
    }

    @Override
    @Transactional
    public void enrollMember(Long courseId, EnrollMemberRequestDTO dto, Long operatorId, List<String> roles) {
        Course course = courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        if (!course.getCreatedBy().equals(operatorId) && !roles.contains("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only creator or admin can enroll members");
        }

        User user = userService.getById(dto.getUserId());
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User not found: " + dto.getUserId());
        }

        Optional<CourseMember> existing = courseMemberMapper.selectByCourseIdAndUserId(courseId, dto.getUserId());
        if (existing.isPresent()) {
            if (Integer.valueOf(1).equals(existing.get().getStatus())) {
                throw new BusinessException(ResultCode.CONFLICT, "User is already a member of this course");
            }
            courseMemberMapper.updateStatusByCourseIdAndUserId(courseId, dto.getUserId(), 1);
            return;
        }

        CourseMember member = new CourseMember();
        member.setCourseId(courseId);
        member.setUserId(dto.getUserId());
        member.setCourseRole(dto.getCourseRole());
        member.setStatus(1);
        courseMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void removeMember(Long courseId, Long userId, Long operatorId, List<String> roles) {
        Course course = courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        if (!course.getCreatedBy().equals(operatorId) && !roles.contains("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only creator or admin can remove members");
        }

        if (course.getCreatedBy().equals(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Cannot remove the course creator");
        }

        courseMemberMapper.updateStatusByCourseIdAndUserId(courseId, userId, 0);
    }

    private CourseVO toCourseVO(Course course) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setCourseCode(course.getCourseCode());
        vo.setCourseName(course.getCourseName());
        vo.setSemester(course.getSemester());
        vo.setDescription(course.getDescription());
        vo.setLocation(course.getLocation());
        vo.setScheduleInfo(course.getScheduleInfo());
        vo.setStatus(course.getStatus());
        vo.setCreatedBy(course.getCreatedBy());
        vo.setCreatedAt(course.getCreatedAt());
        vo.setUpdatedAt(course.getUpdatedAt());
        return vo;
    }

    private CourseDetailVO toCourseDetailVO(Course course) {
        CourseDetailVO vo = new CourseDetailVO();
        vo.setId(course.getId());
        vo.setCourseCode(course.getCourseCode());
        vo.setCourseName(course.getCourseName());
        vo.setSemester(course.getSemester());
        vo.setDescription(course.getDescription());
        vo.setLocation(course.getLocation());
        vo.setScheduleInfo(course.getScheduleInfo());
        vo.setStatus(course.getStatus());
        vo.setCreatedBy(course.getCreatedBy());
        vo.setCreatedAt(course.getCreatedAt());
        vo.setUpdatedAt(course.getUpdatedAt());
        return vo;
    }
}
