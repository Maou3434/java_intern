package com.example.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

/**
 * Entity representing a course.
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

    /**
     * Default constructor.
     */
    public Course() {}

    /**
     * Constructor with title and platform.
     *
     * @param title    Course title.
     * @param platform Platform the course belongs to.
     */
    public Course(String title, Platform platform) {
        this.title = title;
        this.platform = platform;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public Set<User> getUsers() { return users; }

    public void setUsers(Set<User> users) { this.users = users; }

    public Platform getPlatform() { return platform; }

    public void setPlatform(Platform platform) { this.platform = platform; }
}
