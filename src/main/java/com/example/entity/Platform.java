	package com.example.entity;
	
	import jakarta.persistence.*;
	import java.util.Set;
	
	/**
	 * Entity representing a platform.
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

	
	    /**
	     * Default constructor.
	     */
	    public Platform() {}
	
	    /**
	     * Constructor with name.
	     *
	     * @param name Platform name.
	     */
	    public Platform(String name) {
	        this.name = name;
	    }
	
	    public Long getId() { return id; }
	    public void setId(Long id) { this.id = id; }
	
	    public String getName() { return name; }
	    public void setName(String name) { this.name = name; }
	
	    public Set<Course> getCourses() { return courses; }
	    public void setCourses(Set<Course> courses) { this.courses = courses; }
	}
	
