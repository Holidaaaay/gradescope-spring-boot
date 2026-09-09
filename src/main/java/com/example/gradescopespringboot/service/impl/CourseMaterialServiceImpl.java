package com.example.gradescopespringboot.service.impl;

import com.example.gradescopespringboot.common.exception.BusinessException;
import com.example.gradescopespringboot.common.exception.ResourceNotFoundException;
import com.example.gradescopespringboot.common.exception.ResultCode;
import com.example.gradescopespringboot.common.util.FileUtil;
import com.example.gradescopespringboot.dto.material.UploadMaterialRequestDTO;
import com.example.gradescopespringboot.entity.Course;
import com.example.gradescopespringboot.entity.CourseMaterial;
import com.example.gradescopespringboot.entity.CourseMember;
import com.example.gradescopespringboot.mapper.CourseMapper;
import com.example.gradescopespringboot.mapper.CourseMaterialMapper;
import com.example.gradescopespringboot.mapper.CourseMemberMapper;
import com.example.gradescopespringboot.service.CourseMaterialService;
import com.example.gradescopespringboot.service.FileStorageService;
import com.example.gradescopespringboot.vo.material.CourseMaterialVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CourseMaterialServiceImpl implements CourseMaterialService {

    private static final String MATERIAL_SUB_DIRECTORY = "material";

    private final CourseMapper courseMapper;
    private final CourseMemberMapper courseMemberMapper;
    private final CourseMaterialMapper courseMaterialMapper;
    private final FileStorageService fileStorageService;

    public CourseMaterialServiceImpl(CourseMapper courseMapper,
                                     CourseMemberMapper courseMemberMapper,
                                     CourseMaterialMapper courseMaterialMapper,
                                     FileStorageService fileStorageService) {
        this.courseMapper = courseMapper;
        this.courseMemberMapper = courseMemberMapper;
        this.courseMaterialMapper = courseMaterialMapper;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public CourseMaterialVO uploadMaterial(Long courseId, UploadMaterialRequestDTO dto, Long userId, List<String> roles) {
        Course course = courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        checkCourseStaff(course, userId, roles);

        MultipartFile file = dto.getFile();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Uploaded file is empty");
        }

        String fileUrl = fileStorageService.storeFile(file, MATERIAL_SUB_DIRECTORY);

        CourseMaterial material = new CourseMaterial();
        material.setCourseId(courseId);
        material.setTitle(dto.getTitle());
        material.setDescription(dto.getDescription());
        material.setFileName(FileUtil.sanitizeFileName(file.getOriginalFilename()));
        material.setFileUrl(fileUrl);
        material.setFileSize(file.getSize());
        material.setFileType(FileUtil.getExtension(file.getOriginalFilename()));
        material.setUploadedBy(userId);

        courseMaterialMapper.insert(material);
        return toVO(material);
    }

    @Override
    public List<CourseMaterialVO> listMaterialsByCourse(Long courseId, Long userId, List<String> roles) {
        courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        checkCourseMember(courseId, userId, roles);

        return courseMaterialMapper.selectByCourseId(courseId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    public void deleteMaterial(Long courseId, Long materialId, Long userId, List<String> roles) {
        courseMapper.selectById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        CourseMaterial material = courseMaterialMapper.selectById(materialId)
                .filter(m -> m.getCourseId().equals(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + materialId));

        if (!material.getUploadedBy().equals(userId) && !roles.contains("ADMIN")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only uploader or admin can delete this material");
        }

        courseMaterialMapper.deleteById(materialId);
    }

    private void checkCourseMember(Long courseId, Long userId, List<String> roles) {
        if (roles.contains("ADMIN")) {
            return;
        }
        courseMemberMapper.selectByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course"));
    }

    private void checkCourseStaff(Course course, Long userId, List<String> roles) {
        if (roles.contains("ADMIN")) {
            return;
        }
        CourseMember member = courseMemberMapper.selectByCourseIdAndUserId(course.getId(), userId)
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this course"));
        if (!"INSTRUCTOR".equals(member.getCourseRole()) && !"TA".equals(member.getCourseRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only course instructor or TA can manage materials");
        }
    }

    private CourseMaterialVO toVO(CourseMaterial material) {
        CourseMaterialVO vo = new CourseMaterialVO();
        vo.setId(material.getId());
        vo.setCourseId(material.getCourseId());
        vo.setTitle(material.getTitle());
        vo.setDescription(material.getDescription());
        vo.setFileName(material.getFileName());
        vo.setFileUrl(material.getFileUrl());
        vo.setFileSize(material.getFileSize());
        vo.setFileType(material.getFileType());
        vo.setUploadedBy(material.getUploadedBy());
        vo.setCreatedAt(material.getCreatedAt());
        return vo;
    }
}
