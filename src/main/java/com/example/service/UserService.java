package com.example.service;

import com.example.constants.Constants;
import com.example.dto.UserDTO;
import com.example.entity.Course;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.repo.CourseRepository;
import com.example.repo.UserRepository;
import com.example.sync.PlatformSyncService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PlatformSyncService platformSyncService;

    @Autowired
    public UserService(UserRepository userRepository,
                       CourseRepository courseRepository,
                       PlatformSyncService platformSyncService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.platformSyncService = platformSyncService;
    }

    public List<User> getAllUsers(int page, int size) {
        logger.info("Fetching paginated users - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<User> pagedUsers = userRepository.findAll(pageable);

        logger.debug("Found {} users on page {}", pagedUsers.getNumberOfElements(), page);

        return pagedUsers.getContent();
    }


    public User getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found with ID {}", id);
                return new EntityNotFoundException(Constants.NFI + id);
            });
    }

    public User createUser(User user) {
        logger.info("Creating user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("User already exists with email: {}", user.getEmail());
            throw new IllegalArgumentException(Constants.AEE + user.getEmail());
        }

        User saved = userRepository.save(user);
        logger.debug("User created with ID {}", saved.getId());

        platformSyncService.syncAllAffectedPlatforms(saved);
        logger.info("Triggered sync for platforms affected by user ID {}", saved.getId());

        return saved;
    }

    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user with ID {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found with ID {}", id);
                return new EntityNotFoundException(Constants.NFI + id);
            });

        if (userRepository.existsByEmail(userDetails.getEmail()) &&
            !user.getEmail().equals(userDetails.getEmail())) {
            logger.warn("Email already exists for another user: {}", userDetails.getEmail());
            throw new IllegalArgumentException(Constants.AEE + userDetails.getEmail());
        }

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());

        if (userDetails.getCourses() != null) {
            user.setCourses(userDetails.getCourses());
        }

        User updated = userRepository.save(user);
        logger.debug("User updated with ID {}", updated.getId());

        platformSyncService.syncAllAffectedPlatforms(updated);
        logger.info("Triggered sync for platforms affected by updated user ID {}", updated.getId());

        return updated;
    }

    @Transactional
    public UserDTO deleteUser(Long id) {
        logger.info("Deleting user with ID {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found for deletion with ID {}", id);
                return new EntityNotFoundException(Constants.NFI + id);
            });

        UserDTO dto = UserMapper.toDTO(user);

        platformSyncService.syncAllAffectedPlatforms(user);
        logger.info("Triggered sync for platforms to remove user ID {}", id);

        userRepository.deleteById(id);
        logger.debug("Deleted user with ID {}", id);

        return dto;
    }

    @Transactional
    public User enrollUserInCourses(Long userId, Set<Long> courseIds) {
        logger.info("Enrolling user ID {} in courses: {}", userId, courseIds);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.warn("User not found with ID {}", userId);
                return new EntityNotFoundException(Constants.NFI + userId);
            });

        Set<Course> originalCourses = new HashSet<>(user.getCourses());

        if (courseIds == null || courseIds.isEmpty()) {
            logger.debug("Clearing all course enrollments for user ID {}", userId);
            user.getCourses().clear();
        } else {
            List<Course> foundCourses = courseRepository.findAllById(courseIds);

            if (foundCourses.size() != courseIds.size()) {
                Set<Long> foundIds = foundCourses.stream().map(Course::getId).collect(Collectors.toSet());
                Set<Long> missingIds = new HashSet<>(courseIds);
                missingIds.removeAll(foundIds);

                logger.warn("Some course IDs not found for enrollment: {}", missingIds);
                throw new EntityNotFoundException(Constants.NFI + missingIds);
            }

            user.getCourses().clear();
            user.getCourses().addAll(foundCourses);
        }

        User updated = userRepository.save(user);
        logger.debug("Updated course enrollments for user ID {}", userId);

        Set<Course> allAffected = new HashSet<>(originalCourses);
        allAffected.addAll(user.getCourses());

        platformSyncService.syncPlatformsByCourses(allAffected);
        logger.info("Triggered sync for platforms affected by user ID {}'s course updates", userId);

        return updated;
    }
}
