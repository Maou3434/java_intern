package com.example.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for User entity.
 */
public class UserDTO {
    private Long id;
    @NotNull(message= "Name must not be empty")
    private String name;
    @NotNull(message= "email must not be empty")
    private String email;
    private Set<Long> courseIds;

    /**
     * Default constructor.
     */
    public UserDTO() {}

    /**
     * Constructor with fields.
     *
     * @param id
     * @param name
     * @param email
     * @param courseIds
     */
    public UserDTO(Long id, String name, String email, Set<Long> courseIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.courseIds = courseIds;
    }

    /**
     * Gets the user ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the user name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user email.
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the set of course IDs the user is enrolled in.
     */
    public Set<Long> getCourseIds() {
        return courseIds;
    }

    /**
     * Sets the course IDs the user is enrolled in.
     *
     * @param courseIds
     */
    public void setCourseIds(Set<Long> courseIds) {
        this.courseIds = courseIds;
    }
}
