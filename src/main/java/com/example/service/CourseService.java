package com.example.service;

// Constants for common messages
import com.example.constants.Constants;
// Entity classes
import com.example.entity.Course;
import com.example.entity.Platform;
// Repository interfaces
import com.example.repo.CourseRepository;
// Service for syncing platforms to MongoDB
import com.example.sync.PlatformSyncService;

// JPA exception
import jakarta.persistence.EntityNotFoundException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Spring annotations and paging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for managing Course entities.
 * Handles CRUD operations and triggers platform syncs.
 */
@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final PlatformSyncService platformSyncService;

    /**
     * Constructor-based dependency injection.
     *
     * @param courseRepository Repository for Course entities
     * @param platformSyncService Service to sync Platform data to MongoDB
     */
    @Autowired
    public CourseService(CourseRepository courseRepository,
                         PlatformSyncService platformSyncService) {
        this.courseRepository = courseRepository;
        this.platformSyncService = platformSyncService;
    }

    /**
     * Retrieves a paginated list of courses.
     *
     * @param page Page index (0-based)
     * @param size Number of courses per page
     * @return List of courses for the requested page
     */
    public List<Course> getAllCourses(int page, int size) {
        logger.info("Fetching paginated courses");

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> pagedCourses = courseRepository.findAll(pageable);

        logger.debug("Found {} courses on current page", pagedCourses.getNumberOfElements());

        return pagedCourses.getContent();
    }

    /**
     * Retrieves a course by its ID.
     *
     * @param id Course ID
     * @return Course entity if found
     * @throws EntityNotFoundException if course does not exist
     */
    public Course getCourseById(Long id) {
        logger.info("Fetching course");

        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn(Constants.ENTITY_NOT_FOUND);
                    return new EntityNotFoundException(Constants.NOT_FOUND);
                });
    }

    /**
     * Creates a new course.
     * Throws exception if title already exists.
     * Triggers platform sync after creation.
     *
     * @param course Course entity to create
     * @return Created course entity
     * @throws IllegalArgumentException if course title exists
     */
    public Course createCourse(Course course) {
        logger.info("Creating course");

        if (courseRepository.existsByTitle(course.getTitle())) {
            logger.warn("Course title already exists");
            throw new IllegalArgumentException(Constants.ALREADY_EXISTS);
        }

        Course saved = courseRepository.save(course);
        logger.debug("Course created");

        if (saved.getPlatform() != null) {
            logger.info("Syncing platform to Mongo after course creation");
            platformSyncService.syncToMongo(saved.getPlatform());
        }

        return saved;
    }

    /**
     * Updates an existing course by ID.
     * Checks for title uniqueness.
     * Triggers platform sync after update.
     *
     * @param id Course ID to update
     * @param courseDetails Updated course data
     * @return Updated course entity
     * @throws EntityNotFoundException if course does not exist
     * @throws IllegalArgumentException if new title already exists
     */
    public Course updateCourse(Long id, Course courseDetails) {
        logger.info("Updating course");

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn(Constants.ENTITY_NOT_FOUND);
                    return new EntityNotFoundException(Constants.NOT_FOUND);
                });

        if (courseRepository.existsByTitle(courseDetails.getTitle()) &&
                !course.getTitle().equals(courseDetails.getTitle())) {
            logger.warn("Course title already exists");
            throw new IllegalArgumentException(Constants.ALREADY_EXISTS);
        }

        course.setTitle(courseDetails.getTitle());
        Course updated = courseRepository.save(course);
        logger.debug("Updated course");

        if (updated.getPlatform() != null) {
            logger.info("Syncing platform to Mongo after course update");
            platformSyncService.syncToMongo(updated.getPlatform());
        }

        return updated;
    }

    /**
     * Deletes a course by ID.
     * Triggers platform sync after deletion.
     *
     * @param id Course ID to delete
     * @throws EntityNotFoundException if course does not exist
     */
    public void deleteCourse(Long id) {
        logger.info("Deleting course");

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn(Constants.ENTITY_NOT_FOUND);
                    return new EntityNotFoundException(Constants.NOT_FOUND);
                });

        Platform platform = course.getPlatform();
        courseRepository.delete(course);
        logger.debug("Deleted course");

        if (platform != null) {
            logger.info("Syncing platform to Mongo after course deletion");
            platformSyncService.syncToMongo(platform);
        }
    }
}
