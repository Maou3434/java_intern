package com.example.mapper;

// DTO imports
import com.example.dto.CourseDTO;
import com.example.dto.PlatformDTO;

// Document imports (MongoDB)
import com.example.document.PlatformDocument;
import com.example.document.PlatformDocument.CourseEmbed;

// Entity imports (JPA)
import com.example.entity.Course;
import com.example.entity.Platform;

// Java Collections
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility class for mapping between {@link Platform} entity, {@link PlatformDTO} and {@link PlatformDocument}.
 * <p>
 * Provides static methods to convert Platform objects between different layers.
 * </p>
 */
public class PlatformMapper {

    // Private constructor to prevent instantiation
    private PlatformMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a {@link Platform} entity to a {@link PlatformDTO}.
     *
     * @param platform the Platform entity to convert
     * @return the corresponding PlatformDTO or null if input is null
     */
    public static PlatformDTO toDTO(Platform platform) {
        if (platform == null) return null;

        List<CourseDTO> courseDTOs = Collections.emptyList();
        Set<Course> courses = platform.getCourses();
        if (courses != null && !courses.isEmpty()) {
            courseDTOs = courses.stream()
                .map(course -> new CourseDTO(course.getId(), course.getTitle()))
                .toList();
        }

        return new PlatformDTO(
            platform.getId(),
            platform.getName(),
            courseDTOs
        );
    }

    /**
     * Converts a {@link PlatformDocument} (MongoDB document) to a {@link PlatformDTO}.
     *
     * @param platformDoc the PlatformDocument to convert
     * @return the corresponding PlatformDTO or null if input is null
     */
    public static PlatformDTO toDTO(PlatformDocument platformDoc) {
        if (platformDoc == null) return null;

        List<CourseDTO> courseDTOs = Collections.emptyList();
        List<CourseEmbed> courses = platformDoc.getCourses();
        if (courses != null && !courses.isEmpty()) {
            courseDTOs = courses.stream()
                .map(embed -> new CourseDTO(parseId(embed.getId()), embed.getTitle()))
                .toList();
        }

        return new PlatformDTO(
            parseId(platformDoc.getId()),
            platformDoc.getName(),
            courseDTOs
        );
    }

    /**
     * Converts a {@link PlatformDTO} to a {@link Platform} entity.
     *
     * @param dto     the PlatformDTO to convert
     * @param courses the set of courses to associate with the Platform entity
     * @return the corresponding Platform entity or null if dto is null
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

    /**
     * Parses a String ID to Long.
     * 
     * @param idStr the String to parse
     * @return Long value of idStr or null if parsing fails or idStr is null
     */
    private static Long parseId(String idStr) {
        if (idStr == null) return null;
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
