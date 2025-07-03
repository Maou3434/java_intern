package com.example.mapper;

// DTO imports
import com.example.dto.CourseDTO;
// Entity imports
import com.example.entity.Course;

/**
 * Utility class for converting between {@link Course} entity and {@link CourseDTO}.
 * <p>
 * Provides static methods for mapping back and forth between entity and DTO representations.
 * </p>
 */
public class CourseMapper {

    // Private constructor to prevent instantiation of utility class
    private CourseMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a {@link Course} entity to a {@link CourseDTO}.
     *
     * @param course the Course entity to convert
     * @return a CourseDTO with the same id and title, or null if input is null
     */
    public static CourseDTO toDTO(Course course) {
        if (course == null) return null;
        return new CourseDTO(course.getId(), course.getTitle());
    }

    /**
     * Converts a {@link CourseDTO} to a {@link Course} entity.
     *
     * @param dto the CourseDTO to convert
     * @return a Course entity with the same id and title, or null if input is null
     */
    public static Course toEntity(CourseDTO dto) {
        if (dto == null) return null;
        Course course = new Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        return course;
    }
}
