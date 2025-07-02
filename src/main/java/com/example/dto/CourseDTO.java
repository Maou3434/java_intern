package com.example.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for Course entity.
 */
public class CourseDTO {
    private Long id;
    @NotBlank(message = "Title is mandatory")
    private String title;

    /**
     * Default constructor.
     */
    public CourseDTO() {}

    /**
     * Constructor with fields.
     *
     * @param id
     * @param title
     */
    public CourseDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * Gets the course ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the course ID.
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the course title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the course title.
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
