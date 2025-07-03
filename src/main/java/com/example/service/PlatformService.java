package com.example.service;

// DTOs
import com.example.dto.CourseDTO;
import com.example.dto.PlatformDTO;
import com.example.dto.UserDTO;
// Entities
import com.example.entity.Course;
import com.example.entity.Platform;
// Mapper utility
import com.example.mapper.PlatformMapper;
// Constants for messages
import com.example.constants.Constants;
// MongoDB document and embedded classes
import com.example.document.PlatformDocument;
import com.example.document.PlatformDocument.CourseEmbed;
// Repositories
import com.example.repo.CourseRepository;
import com.example.repo.PlatformDocRepository;
import com.example.repo.PlatformRepository;
// Service to sync platform data to MongoDB
import com.example.sync.PlatformSyncService;

// JPA exception
import jakarta.persistence.EntityNotFoundException;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Spring paging, service, and transaction
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing Platform entities and associated data.
 * Handles CRUD operations, syncing between SQL and MongoDB,
 * and resolving embedded courses and users from MongoDB documents.
 */
@Service
public class PlatformService {

    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

    private final PlatformRepository platformRepository;
    private final PlatformDocRepository platformDocRepository;
    private final CourseRepository courseRepository;
    private final PlatformSyncService platformSyncService;

    /**
     * Constructor for PlatformService.
     *
     * @param platformRepository Repository for Platform entities (SQL)
     * @param platformDocRepository Repository for Platform documents (MongoDB)
     * @param courseRepository Repository for Course entities (SQL)
     * @param platformSyncService Service for syncing platform data to MongoDB
     */
    public PlatformService(PlatformRepository platformRepository,
                           PlatformDocRepository platformDocRepository,
                           CourseRepository courseRepository,
                           PlatformSyncService platformSyncService) {
        this.platformRepository = platformRepository;
        this.platformDocRepository = platformDocRepository;
        this.courseRepository = courseRepository;
        this.platformSyncService = platformSyncService;
    }

    /**
     * Retrieves paginated list of platforms.
     *
     * @param page zero-based page index
     * @param size number of items per page
     * @return list of platforms for the requested page
     */
    public List<Platform> getAllPlatforms(int page, int size) {
        logger.info("Fetching paginated platforms");

        Pageable pageable = PageRequest.of(page, size);
        Page<Platform> pagedPlatforms = platformRepository.findAll(pageable);

        logger.debug("Found {} platforms", pagedPlatforms.getNumberOfElements());

        return pagedPlatforms.getContent();
    }

    /**
     * Retrieves a platform by its ID.
     *
     * @param id platform ID
     * @return platform entity if found
     * @throws EntityNotFoundException if platform not found
     */
    public Platform getPlatformById(Long id) {
        logger.info("Fetching platform by ID");

        return platformRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Platform not found");
                return new EntityNotFoundException(Constants.NOT_FOUND + id);
            });
    }

    /**
     * Creates a new platform along with its courses.
     * Links courses back to platform.
     * Triggers asynchronous sync to MongoDB.
     *
     * @param platform platform entity to create
     * @return saved platform entity
     */
    @Transactional
    public Platform createPlatform(Platform platform) {
        logger.info("Creating new platform");

        Set<Course> courses = platform.getCourses();
        if (courses != null) {
            courses.forEach(course -> course.setPlatform(platform));
        }

        Platform saved = platformRepository.save(platform);
        logger.debug("Platform created successfully");

        platformSyncService.syncToMongo(saved);
        logger.info("Triggered async MongoDB sync");

        return saved;
    }

    /**
     * Updates an existing platform and its courses.
     * Syncs updates asynchronously to MongoDB.
     *
     * @param id platform ID to update
     * @param platformDetails platform data with updated fields and courses
     * @return updated platform entity
     */
    @Transactional
    public Platform updatePlatform(Long id, Platform platformDetails) {
        logger.info("Updating platform");

        Platform existing = getPlatformById(id);
        existing.setName(platformDetails.getName());

        Set<Course> existingCourses = existing.getCourses();
        Set<Course> updatedCourses = platformDetails.getCourses();

        if (existingCourses == null) {
            existingCourses = new HashSet<>();
            existing.setCourses(existingCourses);
        }

        // Remove courses no longer present
        existingCourses.removeIf(course ->
            updatedCourses.stream().noneMatch(updated -> updated.getId().equals(course.getId()))
        );

        // Update existing or add new courses
        for (Course updatedCourse : updatedCourses) {
            Course existingCourse = existingCourses.stream()
                .filter(c -> c.getId().equals(updatedCourse.getId()))
                .findFirst()
                .orElse(null);

            if (existingCourse != null) {
                existingCourse.setTitle(updatedCourse.getTitle());
                existingCourse.setPlatform(existing);
            } else {
                updatedCourse.setPlatform(existing);
                existingCourses.add(updatedCourse);
            }
        }

        Platform updated = platformRepository.save(existing);
        logger.debug("Platform updated successfully");

        platformSyncService.syncToMongo(updated);
        logger.info("Triggered async MongoDB sync");

        return updated;
    }

    /**
     * Deletes a platform by ID.
     * Triggers async delete in MongoDB.
     *
     * @param id platform ID to delete
     * @return DTO of deleted platform
     */
    @Transactional
    public PlatformDTO deletePlatformById(Long id) {
        logger.info("Deleting platform");

        Platform existing = getPlatformById(id);
        PlatformDTO dto = PlatformMapper.toDTO(existing);

        platformRepository.delete(existing);
        logger.debug("Platform deleted from SQL");

        platformSyncService.deletePlatformFromMongo(id);
        logger.info("Triggered async MongoDB delete");

        return dto;
    }

    /**
     * Resolves courses from a PlatformDTO by fetching from SQL.
     *
     * @param dto PlatformDTO containing course IDs
     * @return set of Course entities
     * @throws EntityNotFoundException if any course IDs are not found
     */
    public Set<Course> getCoursesByDTO(PlatformDTO dto) {
        logger.info("Resolving courses from DTO");

        if (dto == null || dto.getCourses() == null || dto.getCourses().isEmpty()) {
            logger.debug("No courses found in DTO");
            return Collections.emptySet();
        }

        Set<Long> courseIds = dto.getCourses().stream()
                .map(CourseDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (courseIds.isEmpty()) {
            logger.debug("No valid course IDs found");
            return Collections.emptySet();
        }

        List<Course> foundCourses = courseRepository.findAllById(courseIds);

        if (foundCourses.size() != courseIds.size()) {
            Set<Long> foundIds = foundCourses.stream().map(Course::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(courseIds);
            missingIds.removeAll(foundIds);
            logger.warn("Some course IDs not found");
            throw new EntityNotFoundException(Constants.NOT_FOUND + missingIds);
        }

        return new HashSet<>(foundCourses);
    }

    /**
     * Fetches users enrolled in a platform's courses from MongoDB document.
     *
     * @param platformDocId MongoDB document ID of platform
     * @return list of UserDTOs with enrolled course IDs
     * @throws EntityNotFoundException if platform document not found
     */
    public List<UserDTO> getUsersByPlatformIdFromMongo(String platformDocId) {
        logger.info("Fetching users from MongoDB");

        PlatformDocument doc = platformDocRepository.findById(platformDocId)
                .orElseThrow(() -> {
                    logger.warn("Platform document not found");
                    return new EntityNotFoundException(Constants.NOT_FOUND + platformDocId);
                });

        List<CourseEmbed> courses = doc.getCourses();
        if (courses == null || courses.isEmpty()) {
            logger.debug("No courses found in platform document");
            return Collections.emptyList();
        }

        Map<String, Set<String>> userToCourseIds = new HashMap<>();

        for (CourseEmbed course : courses) {
            String courseId = course.getId();
            List<PlatformDocument.UserEmbed> enrolledUsers = course.getEnrolledUsers();

            if (enrolledUsers == null) continue;

            for (PlatformDocument.UserEmbed user : enrolledUsers) {
                userToCourseIds.computeIfAbsent(user.getId(), k -> new HashSet<>()).add(courseId);
            }
        }

        Map<String, PlatformDocument.UserEmbed> uniqueUsers = new HashMap<>();
        for (CourseEmbed course : courses) {
            if (course.getEnrolledUsers() == null) continue;
            for (PlatformDocument.UserEmbed user : course.getEnrolledUsers()) {
                uniqueUsers.putIfAbsent(user.getId(), user);
            }
        }

        return uniqueUsers.values().stream()
                .map(u -> new UserDTO(
                        parseId(u.getId()),
                        u.getName(),
                        u.getEmail(),
                        userToCourseIds.getOrDefault(u.getId(), Collections.emptySet())
                                .stream()
                                .map(this::parseId)
                                .collect(Collectors.toSet())
                )).toList();
    }

    /**
     * Fetches courses embedded in a platform's MongoDB document.
     *
     * @param platformDocId MongoDB document ID of platform
     * @return list of CourseDTOs
     * @throws EntityNotFoundException if platform document not found
     */
    public List<CourseDTO> getCoursesByPlatformIdFromMongo(String platformDocId) {
        logger.info("Fetching courses from MongoDB");

        PlatformDocument doc = platformDocRepository.findById(platformDocId)
                .orElseThrow(() -> {
                    logger.warn("Platform document not found");
                    return new EntityNotFoundException(Constants.NOT_FOUND + platformDocId);
                });

        List<CourseEmbed> courses = doc.getCourses();
        if (courses == null) {
            logger.debug("No embedded courses in platform document");
            return Collections.emptyList();
        }

        return courses.stream()
                .map(c -> new CourseDTO(
                        parseId(c.getId()),
                        c.getTitle()
                )).toList();
    }

    /**
     * Parses a string ID to Long, returns null on failure.
     *
     * @param idStr string representation of ID
     * @return Long or null if parsing fails
     */
    private Long parseId(String idStr) {
        try {
            return idStr == null ? null : Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse ID string");
            return null;
        }
    }
}
