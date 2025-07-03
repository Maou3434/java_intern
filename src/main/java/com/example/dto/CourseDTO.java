package com.example.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for transferring Course data.
 */
public class CourseDTO {
    private Long id;

    @NotBlank(message = "Title is mandatory")
    private String title;

    /** Default constructor */
    public CourseDTO() {}

    /**
     * Constructor with all fields.
     * 
     * @param id Course ID
     * @param title Course title
     */
    public CourseDTO(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    /** Returns the course ID. */
    public Long getId() {
        return id;
    }

    /** Sets the course ID. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the course title. */
    public String getTitle() {
        return title;
    }

    /** Sets the course title. */
    public void setTitle(String title) {
        this.title = title;
    }
}
