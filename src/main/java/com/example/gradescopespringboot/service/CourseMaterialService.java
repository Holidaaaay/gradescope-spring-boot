package com.example.gradescopespringboot.service;

import com.example.gradescopespringboot.dto.material.UploadMaterialRequestDTO;
import com.example.gradescopespringboot.vo.material.CourseMaterialVO;

import java.util.List;

public interface CourseMaterialService {

    /**
     * Upload a course material file. Only course instructor/TA or admin.
     *
     * @param courseId course id
     * @param dto      upload data including multipart file
     * @param userId   uploader user id
     * @param roles    uploader roles
     * @return created material
     */
    CourseMaterialVO uploadMaterial(Long courseId, UploadMaterialRequestDTO dto, Long userId, List<String> roles);

    /**
     * List materials of a course. All course members can view.
     *
     * @param courseId course id
     * @param userId   user id
     * @param roles    user roles
     * @return material list
     */
    List<CourseMaterialVO> listMaterialsByCourse(Long courseId, Long userId, List<String> roles);

    /**
     * Logically delete a course material. Only uploader or admin.
     *
     * @param courseId   course id
     * @param materialId material id
     * @param userId     operator user id
     * @param roles      operator roles
     */
    void deleteMaterial(Long courseId, Long materialId, Long userId, List<String> roles);
}
