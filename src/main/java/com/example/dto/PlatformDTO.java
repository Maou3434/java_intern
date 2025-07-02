package com.example.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for Platform entity.
 */
public class PlatformDTO {
    private Long id;
    @NotNull(message = "must not be empty")   
    private String name;
    private List<CourseDTO> courses;

    /**
     * Default constructor.
     */
    public PlatformDTO() {}

    /**
     * Constructor with fields.
     *
     * @param id
     * @param name
     * @param courses
     */
    public PlatformDTO(Long id, String name, List<CourseDTO> courses) {
        this.id = id;
        this.name = name;
        this.courses = courses;
    }

    /**
     * Gets the platform ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the platform ID.
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the platform name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the platform name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of courses.
     */
    public List<CourseDTO> getCourses() {
        return courses;
    }

    /**
     * Sets the list of courses.
     *
     * @param courses
     */
    public void setCourses(List<CourseDTO> courses) {
        this.courses = courses;
    }
}
