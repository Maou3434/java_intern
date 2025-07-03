package com.example.entity;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Entity representing a User.
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

    /** Default constructor. */
    public User() {}

    /**
     * Constructs a User with the specified name and email.
     *
     * @param name  the user's name
     * @param email the user's unique email
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /** Returns the user ID. */
    public Long getId() {
        return id;
    }

    /** Sets the user ID. */
    public void setId(Long id) {
        this.id = id;
    }

    /** Returns the user's name. */
    public String getName() {
        return name;
    }

    /** Sets the user's name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the user's email. */
    public String getEmail() {
        return email;
    }

    /** Sets the user's email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns the set of courses the user is enrolled in. */
    public Set<Course> getCourses() {
        return courses;
    }

    /** Sets the courses the user is enrolled in. */
    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }
}
