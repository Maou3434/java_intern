package com.example.mapper;

import com.example.dto.CourseDTO;
import com.example.dto.PlatformDTO;
import com.example.document.PlatformDocument;
import com.example.document.PlatformDocument.CourseEmbed;
import com.example.entity.Course;
import com.example.entity.Platform;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Platform entity/document and PlatformDTO.
 */
public class PlatformMapper {

    /**
     * Converts Platform entity to PlatformDTO.
     *
     * @param platform JPA entity
     * @return PlatformDTO or null if platform is null
     */
    public static PlatformDTO toDTO(Platform platform) {
        if (platform == null) return null;

        List<CourseDTO> courseDTOs = Collections.emptyList();
        Set<Course> courses = platform.getCourses();
        if (courses != null && !courses.isEmpty()) {
            courseDTOs = courses.stream()
                .map(course -> new CourseDTO(course.getId(), course.getTitle()))
                .collect(Collectors.toList());
        }

        return new PlatformDTO(
            platform.getId(),
            platform.getName(),
            courseDTOs
        );
    }

    /**
     * Converts PlatformDocument (Mongo) to PlatformDTO.
     *
     * @param platformDoc MongoDB document
     * @return PlatformDTO or null if platformDoc is null
     */
    public static PlatformDTO toDTO(PlatformDocument platformDoc) {
        if (platformDoc == null) return null;

        List<CourseDTO> courseDTOs = Collections.emptyList();
        List<CourseEmbed> courses = platformDoc.getCourses();
        if (courses != null && !courses.isEmpty()) {
            courseDTOs = courses.stream()
                .map(embed -> new CourseDTO(parseId(embed.getId()), embed.getTitle()))
                .collect(Collectors.toList());
        }

        return new PlatformDTO(
            parseId(platformDoc.getId()),
            platformDoc.getName(),
            courseDTOs
        );
    }

    /**
     * Converts PlatformDTO to Platform entity.
     *
     * @param dto PlatformDTO
     * @param courses Set of Course entities to associate with Platform
     * @return Platform entity
     */
    public static Platform toEntity(PlatformDTO dto, Set<Course> courses) {
        if (dto == null) return null;

        Platform platform = new Platform();
        if (dto.getId() != null) {
            platform.setId(dto.getId());
        }
        platform.setName(dto.getName());
        platform.setCourses(courses != null ? courses : Collections.emptySet());

        return platform;
    }

    private static Long parseId(String idStr) {
        if (idStr == null) return null;
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
