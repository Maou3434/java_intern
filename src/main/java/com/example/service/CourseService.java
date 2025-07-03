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
        logger.info("Fetching paginated courses");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> pagedCourses = courseRepository.findAll(pageable);
        
        logger.debug("Found {} courses on current page", pagedCourses.getNumberOfElements());
        
        return pagedCourses.getContent();
    }


    public Course getCourseById(Long id) {
        logger.info("Fetching course");

        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found");
                    return new EntityNotFoundException(Constants.NFI);
                });
    }

    public Course createCourse(Course course) {
        logger.info("Creating course");

        if (courseRepository.existsByTitle(course.getTitle())) {
            logger.warn("Course title already exists");
            throw new IllegalArgumentException(Constants.AEE);
        }

        Course saved = courseRepository.save(course);
        logger.debug("Course created");

        if (saved.getPlatform() != null) {
            logger.info("Syncing platform to Mongo after course creation");
            platformSyncService.syncToMongo(saved.getPlatform());
        }

        return saved;
    }

    public Course updateCourse(Long id, Course courseDetails) {
        logger.info("Updating course");

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found");
                    return new EntityNotFoundException(Constants.NFI);
                });

        if (courseRepository.existsByTitle(courseDetails.getTitle()) &&
                !course.getTitle().equals(courseDetails.getTitle())) {
            logger.warn("Course title already exists");
            throw new IllegalArgumentException(Constants.AEE);
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

    public void deleteCourse(Long id) {
        logger.info("Deleting course");

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Course not found");
                    return new EntityNotFoundException(Constants.NFI);
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
