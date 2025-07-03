package com.example.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

/**
 * Entity representing a Course.
 */
@Entity
@Table(
    name = "courses",
    uniqueConstraints = @UniqueConstraint(columnNames = "title")
)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @ManyToMany(mappedBy = "courses")
    @JsonIgnore
    private Set<User> users;

    @ManyToOne
    @JoinColumn(name = "platform_id", nullable = true)
    private Platform platform;

    /** Default constructor */
    public Course() {}

    /**
     * Constructs a Course with given title and platform.
     *
     * @param title Course title
     * @param platform Associated platform
     */
    public Course(String title, Platform platform) {
        this.title = title;
        this.platform = platform;
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

    /** Returns users enrolled in the course. */
    public Set<User> getUsers() {
        return users;
    }

    /** Sets the users enrolled in the course. */
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    /** Returns the platform associated with the course. */
    public Platform getPlatform() {
        return platform;
    }

    /** Sets the platform associated with the course. */
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
}
