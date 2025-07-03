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
        logger.info("Fetching paginated users");

        Pageable pageable = PageRequest.of(page, size);
        Page<User> pagedUsers = userRepository.findAll(pageable);

        logger.debug("Found {} users", pagedUsers.getNumberOfElements());

        return pagedUsers.getContent();
    }

    public User getUserById(Long id) {
        logger.info("Fetching user by ID");

        return userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found");
                return new EntityNotFoundException(Constants.NFI + id);
            });
    }

    public User createUser(User user) {
        logger.info("Creating user");

        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("User already exists with given email");
            throw new IllegalArgumentException(Constants.AEE + user.getEmail());
        }

        User saved = userRepository.save(user);
        logger.debug("User created successfully");

        platformSyncService.syncAllAffectedPlatforms(saved);
        logger.info("Triggered platform sync after user creation");

        return saved;
    }

    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user");

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found");
                return new EntityNotFoundException(Constants.NFI + id);
            });

        if (userRepository.existsByEmail(userDetails.getEmail()) &&
            !user.getEmail().equals(userDetails.getEmail())) {
            logger.warn("Email already in use by another user");
            throw new IllegalArgumentException(Constants.AEE + userDetails.getEmail());
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

    @Transactional
    public UserDTO deleteUser(Long id) {
        logger.info("Deleting user");

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found for deletion");
                return new EntityNotFoundException(Constants.NFI + id);
            });

        UserDTO dto = UserMapper.toDTO(user);

        platformSyncService.syncAllAffectedPlatforms(user);
        logger.info("Triggered platform sync for user deletion");

        userRepository.deleteById(id);
        logger.debug("User deleted successfully");

        return dto;
    }

    @Transactional
    public User enrollUserInCourses(Long userId, Set<Long> courseIds) {
        logger.info("Enrolling user in courses");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.warn("User not found");
                return new EntityNotFoundException(Constants.NFI + userId);
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
                throw new EntityNotFoundException(Constants.NFI + missingIds);
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
