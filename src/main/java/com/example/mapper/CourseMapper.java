package com.example.mapper;

import com.example.dto.CourseDTO;
import com.example.entity.Course;

/**
 * Mapper class for converting between Course entity and CourseDTO.
 */
public class CourseMapper {
	
	private CourseMapper() {
		throw new IllegalStateException("This is a utility class");
	}

    /**
     * Converts Course entity to CourseDTO.
     *
     * @param course
     */
    public static CourseDTO toDTO(Course course) {
        if (course == null) return null;
        return new CourseDTO(course.getId(), course.getTitle());
    }

    /**
     * Converts CourseDTO to Course entity.
     *
     * @param dto
     */
    public static Course toEntity(CourseDTO dto) {
        if (dto == null) return null;
        Course course = new Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        return course;
    }
}
