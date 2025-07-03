package com.example.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for transferring Platform data.
 */
public class PlatformDTO {
    private Long id;

    @NotNull(message = "must not be empty")
    private String name;

    private List<CourseDTO> courses;

    /** Default constructor */
    public PlatformDTO() {}

    /**
     * Constructor with all fields.
     * 
     * @param id Platform ID
     * @param name Platform name
     * @param courses List of courses
     */
    public PlatformDTO(Long id, String name, List<CourseDTO> courses) {
        this.id = id;
        this.name = name;
        this.courses = courses;
    }

    /** Returns the platform ID. */
    public Long getId() {
        return id;
    }

    /** Sets the platform ID. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the platform name. */
    public String getName() {
        return name;
    }

    /** Sets the platform name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the list of courses. */
    public List<CourseDTO> getCourses() {
        return courses;
    }

    /** Sets the list of courses. */
    public void setCourses(List<CourseDTO> courses) {
        this.courses = courses;
    }
}
