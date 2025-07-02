package com.example.entity;

import jakarta.persistence.*;

import java.util.Set;

/**
 * Entity representing a user.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
        name = "user_course",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses;

    /**
     * Default constructor.
     */
    public User() {}

    /**
     * Constructor with name and email.
     *
     * @param name
     * @param email
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Gets the user ID.
     */
    public Long getId() { return id; }

    /**
     * Sets the user ID.
     *
     * @param id
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Gets the user name.
     */
    public String getName() { return name; }

    /**
     * Sets the user name.
     *
     * @param name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the user email.
     */
    public String getEmail() { return email; }

    /**
     * Sets the user email.
     *
     * @param email
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the courses the user is enrolled in.
     */
    public Set<Course> getCourses() { return courses; }

    /**
     * Sets the courses the user is enrolled in.
     *
     * @param courses
     */
    public void setCourses(Set<Course> courses) { this.courses = courses; }
}
