package com.example.service;

import com.example.constants.Constants;
import com.example.entity.Course;
import com.example.entity.Platform;
import com.example.repo.CourseRepository;
import com.example.sync.PlatformSyncService;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final PlatformSyncService platformSyncService;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         PlatformSyncService platformSyncService) {
        this.courseRepository = courseRepository;
        this.platformSyncService = platformSyncService;
    }

    public List<Course> getAllCourses(int page, int size) {
        logger.info("Fetching paginated courses - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> pagedCourses = courseRepository.findAll(pageable);
        
        logger.debug("Found {} courses on current page", pagedCourses.getNumberOfElements());
        
        return pagedCourses.getContent(); // returns only the current page content
    }


    public Course getCourseById(Long id) {
        logger.info("Fetching course with ID {}", id);
        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID {}", id);
                    return new EntityNotFoundException(Constants.NFI + id);
                });
    }

    public Course createCourse(Course course) {
        logger.info("Creating course with title '{}'", course.getTitle());

        if (courseRepository.existsByTitle(course.getTitle())) {
            logger.warn("Course title '{}' already exists", course.getTitle());
            throw new IllegalArgumentException(Constants.AEE + course.getTitle());
        }

        Course saved = courseRepository.save(course);
        logger.debug("Course created with ID {}", saved.getId());

        if (saved.getPlatform() != null) {
            logger.info("Syncing platform ID {} to Mongo after course creation", saved.getPlatform().getId());
            platformSyncService.syncToMongo(saved.getPlatform());
        }

        return saved;
    }

    public Course updateCourse(Long id, Course courseDetails) {
        logger.info("Updating course with ID {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID {}", id);
                    return new EntityNotFoundException(Constants.NFI + id);
                });

        if (courseRepository.existsByTitle(courseDetails.getTitle()) &&
                !course.getTitle().equals(courseDetails.getTitle())) {
            logger.warn("Course title '{}' already exists", courseDetails.getTitle());
            throw new IllegalArgumentException(Constants.AEE + courseDetails.getTitle());
        }

        course.setTitle(courseDetails.getTitle());
        Course updated = courseRepository.save(course);
        logger.debug("Updated course with ID {}", updated.getId());

        if (updated.getPlatform() != null) {
            logger.info("Syncing platform ID {} to Mongo after course update", updated.getPlatform().getId());
            platformSyncService.syncToMongo(updated.getPlatform());
        }

        return updated;
    }

    public void deleteCourse(Long id) {
        logger.info("Deleting course with ID {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found with ID {}", id);
                    return new EntityNotFoundException(Constants.NFI + id);
                });

        Platform platform = course.getPlatform(); // capture before deletion
        courseRepository.delete(course);
        logger.debug("Deleted course with ID {}", id);

        if (platform != null) {
            logger.info("Syncing platform ID {} to Mongo after course deletion", platform.getId());
            platformSyncService.syncToMongo(platform);
        }
    }
}
