package com.example.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for transferring User data.
 */
public class UserDTO {
    private Long id;

    @NotNull(message = "Name must not be empty")
    private String name;

    @NotNull(message = "email must not be empty")
    private String email;

    private Set<Long> courseIds;

    /** Default constructor */
    public UserDTO() {}

    /**
     * Constructor with all fields.
     * 
     * @param id User ID
     * @param name User name
     * @param email User email
     * @param courseIds IDs of courses enrolled
     */
    public UserDTO(Long id, String name, String email, Set<Long> courseIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.courseIds = courseIds;
    }

    /** Returns the user ID. */
    public Long getId() {
        return id;
    }

    /** Sets the user ID. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the user name. */
    public String getName() {
        return name;
    }

    /** Sets the user name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the user email. */
    public String getEmail() {
        return email;
    }

    /** Sets the user email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns course IDs the user is enrolled in. */
    public Set<Long> getCourseIds() {
        return courseIds;
    }

    /** Sets course IDs the user is enrolled in. */
    public void setCourseIds(Set<Long> courseIds) {
        this.courseIds = courseIds;
    }
}
