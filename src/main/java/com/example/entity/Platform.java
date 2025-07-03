package com.example.entity;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Entity representing a Platform.
 */
@Entity
@Table(
    name = "platforms",
    uniqueConstraints = @UniqueConstraint(columnNames = "name")
)
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "platform", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Course> courses;

    /** Default constructor */
    public Platform() {}

    /**
     * Constructs a Platform with the given name.
     *
     * @param name Platform name
     */
    public Platform(String name) {
        this.name = name;
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

    /** Returns the set of courses under the platform. */
    public Set<Course> getCourses() {
        return courses;
    }

    /** Sets the courses under the platform. */
    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }
}
