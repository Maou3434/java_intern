package com.example.service;

// Constants for messages
import com.example.constants.Constants;
// DTOs and mappers
import com.example.dto.UserDTO;
import com.example.mapper.UserMapper;
// Entities
import com.example.entity.Course;
import com.example.entity.User;
// Repositories
import com.example.repo.CourseRepository;
import com.example.repo.UserRepository;
// Sync service for platforms
import com.example.sync.PlatformSyncService;

// Exceptions and transactions
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

// Logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Spring annotations and pagination
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing User entities,
 * including CRUD operations, course enrollment,
 * and triggering platform synchronization.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PlatformSyncService platformSyncService;

    /**
     * Constructor for UserService.
     *
     * @param userRepository repository for User entities
     * @param courseRepository repository for Course entities
     * @param platformSyncService service to sync affected platforms
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       CourseRepository courseRepository,
                       PlatformSyncService platformSyncService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.platformSyncService = platformSyncService;
    }

    /**
     * Retrieves paginated users.
     *
     * @param page zero-based page index
     * @param size number of users per page
     * @return list of users in the requested page
     */
    public List<User> getAllUsers(int page, int size) {
        logger.info("Fetching paginated users");

        Pageable pageable = PageRequest.of(page, size);
        Page<User> pagedUsers = userRepository.findAll(pageable);

        logger.debug("Found {} users", pagedUsers.getNumberOfElements());

        return pagedUsers.getContent();
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id user ID
     * @return user entity if found
     * @throws EntityNotFoundException if user not found
     */
    public User getUserById(Long id) {
        logger.info("Fetching user by ID");

        return userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn(Constants.ENTITY_NOT_FOUND);
                return new EntityNotFoundException(Constants.NOT_FOUND + id);
            });
    }

    /**
     * Creates a new user.
     * Checks for existing email to prevent duplicates.
     * Triggers platform sync after creation.
     *
     * @param user user entity to create
     * @return created user entity
     * @throws IllegalArgumentException if email already exists
     */
    public User createUser(User user) {
        logger.info("Creating user");

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("User already exists with given email");
            throw new IllegalArgumentException(Constants.ALREADY_EXISTS + user.getEmail());
        }

        User saved = userRepository.save(user);
        logger.debug("User created successfully");

        platformSyncService.syncAllAffectedPlatforms(saved);
        logger.info("Triggered platform sync after user creation");

        return saved;
    }

    /**
     * Updates existing user details.
     * Prevents email duplication.
     * Triggers platform sync after update.
     *
     * @param id user ID to update
     * @param userDetails user entity containing updated data
     * @return updated user entity
     * @throws EntityNotFoundException if user not found
     * @throws IllegalArgumentException if new email already exists on another user
     */
    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user");

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn(Constants.ENTITY_NOT_FOUND);
                return new EntityNotFoundException(Constants.NOT_FOUND + id);
            });

        if (userRepository.existsByEmail(userDetails.getEmail()) &&
            !user.getEmail().equals(userDetails.getEmail())) {
            logger.warn("Email already in use by another user");
            throw new IllegalArgumentException(Constants.ALREADY_EXISTS + userDetails.getEmail());
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        if (userDetails.getCourses() != null) {
            user.setCourses(userDetails.getCourses());
        }

        User updated = userRepository.save(user);
        logger.debug("User updated successfully");

        platformSyncService.syncAllAffectedPlatforms(updated);
        logger.info("Triggered platform sync after user update");

        return updated;
    }

    /**
     * Deletes a user by ID.
     * Triggers platform sync after deletion.
     *
     * @param id user ID to delete
     * @return DTO representation of deleted user
     * @throws EntityNotFoundException if user not found
     */
    @Transactional
    public UserDTO deleteUser(Long id) {
        logger.info("Deleting user");

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn(Constants.ENTITY_NOT_FOUND);
                return new EntityNotFoundException(Constants.NOT_FOUND + id);
            });

        UserDTO dto = UserMapper.toDTO(user);

        platformSyncService.syncAllAffectedPlatforms(user);
        logger.info("Triggered platform sync for user deletion");

        userRepository.deleteById(id);
        logger.debug("User deleted successfully");

        return dto;
    }

    /**
     * Enrolls a user in a set of courses.
     * Validates course IDs and updates user enrollments.
     * Triggers platform sync for all affected courses.
     *
     * @param userId ID of user to enroll
     * @param courseIds set of course IDs to enroll the user in
     * @return updated user entity with enrolled courses
     * @throws EntityNotFoundException if any course IDs are invalid
     */
    @Transactional
    public User enrollUserInCourses(Long userId, Set<Long> courseIds) {
        logger.info("Enrolling user in courses");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.warn(Constants.ENTITY_NOT_FOUND);
                return new EntityNotFoundException(Constants.NOT_FOUND + userId);
            });

        Set<Course> originalCourses = new HashSet<>(user.getCourses());

        if (courseIds == null || courseIds.isEmpty()) {
            logger.debug("Clearing course enrollments for user");
            user.getCourses().clear();
        } else {
            List<Course> foundCourses = courseRepository.findAllById(courseIds);

            if (foundCourses.size() != courseIds.size()) {
                Set<Long> foundIds = foundCourses.stream().map(Course::getId).collect(Collectors.toSet());
                Set<Long> missingIds = new HashSet<>(courseIds);
                missingIds.removeAll(foundIds);

                logger.warn("Some course IDs not found for enrollment");
                throw new EntityNotFoundException(Constants.NOT_FOUND + missingIds);
            }

            user.getCourses().clear();
            user.getCourses().addAll(foundCourses);
        }

        User updated = userRepository.save(user);
        logger.debug("Course enrollments updated for user");

        Set<Course> allAffected = new HashSet<>(originalCourses);
        allAffected.addAll(user.getCourses());

        platformSyncService.syncPlatformsByCourses(allAffected);
        logger.info("Triggered platform sync after course enrollment");

        return updated;
    }
}
