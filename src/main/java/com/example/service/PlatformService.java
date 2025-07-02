package com.example.service;

import com.example.dto.CourseDTO;
import com.example.dto.PlatformDTO;
import com.example.dto.UserDTO;
import com.example.entity.Course;
import com.example.entity.Platform;
import com.example.mapper.PlatformMapper;
import com.example.constants.Constants;
import com.example.document.PlatformDocument;
import com.example.document.PlatformDocument.CourseEmbed;
import com.example.repo.CourseRepository;
import com.example.repo.PlatformDocRepository;
import com.example.repo.PlatformRepository;
import com.example.sync.PlatformSyncService;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlatformService {

    private static final Logger logger = LoggerFactory.getLogger(PlatformService.class);

    private final PlatformRepository platformRepository;
    private final PlatformDocRepository platformDocRepository;
    private final CourseRepository courseRepository;
    private final PlatformSyncService platformSyncService;

    public PlatformService(PlatformRepository platformRepository,
                           PlatformDocRepository platformDocRepository,
                           CourseRepository courseRepository,
                           PlatformSyncService platformSyncService) {
        this.platformRepository = platformRepository;
        this.platformDocRepository = platformDocRepository;
        this.courseRepository = courseRepository;
        this.platformSyncService = platformSyncService;
    }

    public List<Platform> getAllPlatforms(int page, int size) {
        logger.info("Fetching paginated platforms - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Platform> pagedPlatforms = platformRepository.findAll(pageable);

        logger.debug("Found {} platforms on page {}", pagedPlatforms.getNumberOfElements(), page);

        return pagedPlatforms.getContent();
    }


    public Platform getPlatformById(Long id) {
        logger.info("Fetching platform by ID: {}", id);
        return platformRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("Platform not found with ID {}", id);
                return new EntityNotFoundException(Constants.NFI + id);
            });
    }

    @Transactional
    public Platform createPlatform(Platform platform) {
        logger.info("Creating platform: {}", platform.getName());

        Set<Course> courses = platform.getCourses();
        if (courses != null) {
            courses.forEach(course -> course.setPlatform(platform));
        }

        Platform saved = platformRepository.save(platform);
        logger.debug("Platform created with ID {}", saved.getId());

        platformSyncService.syncToMongo(saved);
        logger.info("Triggered async MongoDB sync for platform ID {}", saved.getId());

        return saved;
    }

    @Transactional
    public Platform updatePlatform(Long id, Platform platformDetails) {
        logger.info("Updating platform with ID {}", id);

        Platform existing = getPlatformById(id);
        existing.setName(platformDetails.getName());

        Set<Course> existingCourses = existing.getCourses();
        Set<Course> updatedCourses = platformDetails.getCourses();

        if (existingCourses == null) {
            existingCourses = new HashSet<>();
            existing.setCourses(existingCourses);
        }

        existingCourses.removeIf(course ->
            updatedCourses.stream().noneMatch(updated -> updated.getId().equals(course.getId()))
        );

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
        logger.debug("Platform updated with ID {}", updated.getId());

        platformSyncService.syncToMongo(updated);
        logger.info("Triggered async MongoDB sync for platform ID {}", updated.getId());

        return updated;
    }

    @Transactional
    public PlatformDTO deletePlatformById(Long id) {
        logger.info("Deleting platform with ID {}", id);

        Platform existing = getPlatformById(id);
        PlatformDTO dto = PlatformMapper.toDTO(existing);

        platformRepository.delete(existing);
        logger.debug("Deleted platform from SQL with ID {}", id);

        platformSyncService.deletePlatformFromMongo(id);
        logger.info("Triggered async MongoDB delete for platform ID {}", id);

        return dto;
    }

    public Set<Course> getCoursesByDTO(PlatformDTO dto) {
        logger.info("Fetching courses by DTO");

        if (dto == null || dto.getCourses() == null || dto.getCourses().isEmpty()) {
            logger.debug("No courses provided in DTO");
            return Collections.emptySet();
        }

        Set<Long> courseIds = dto.getCourses().stream()
                .map(CourseDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (courseIds.isEmpty()) {
            logger.debug("No valid course IDs found in DTO");
            return Collections.emptySet();
        }

        List<Course> foundCourses = courseRepository.findAllById(courseIds);

        if (foundCourses.size() != courseIds.size()) {
            Set<Long> foundIds = foundCourses.stream().map(Course::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(courseIds);
            missingIds.removeAll(foundIds);
            logger.warn("Missing courses with IDs: {}", missingIds);
            throw new EntityNotFoundException(Constants.NFI + missingIds);
        }

        return new HashSet<>(foundCourses);
    }

    public List<UserDTO> getUsersByPlatformIdFromMongo(String platformDocId) {
        logger.info("Fetching users for platform document ID {}", platformDocId);

        PlatformDocument doc = platformDocRepository.findById(platformDocId)
                .orElseThrow(() -> {
                    logger.warn("Platform document not found with ID {}", platformDocId);
                    return new EntityNotFoundException(Constants.NFI + platformDocId);
                });

        List<CourseEmbed> courses = doc.getCourses();
        if (courses == null || courses.isEmpty()) {
            logger.debug("No courses found in platform document ID {}", platformDocId);
            return Collections.emptyList();
        }

        Map<String, Set<String>> userToCourseIds = new HashMap<>();

        for (CourseEmbed course : courses) {
            String courseId = course.getId();
            List<PlatformDocument.UserEmbed> enrolledUsers = course.getEnrolledUsers();

            if (enrolledUsers == null) continue;

            for (PlatformDocument.UserEmbed user : enrolledUsers) {
                userToCourseIds.computeIfAbsent(user.getId(), k -> new HashSet<>())
                               .add(courseId);
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
                ))
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByPlatformIdFromMongo(String platformDocId) {
        logger.info("Fetching courses for platform document ID {}", platformDocId);

        PlatformDocument doc = platformDocRepository.findById(platformDocId)
                .orElseThrow(() -> {
                    logger.warn("Platform document not found with ID {}", platformDocId);
                    return new EntityNotFoundException(Constants.NFI + platformDocId);
                });

        List<CourseEmbed> courses = doc.getCourses();
        if (courses == null) {
            logger.debug("No courses embedded in document ID {}", platformDocId);
            return Collections.emptyList();
        }

        return courses.stream()
                .map(c -> new CourseDTO(
                        parseId(c.getId()),
                        c.getTitle()
                ))
                .collect(Collectors.toList());
    }

    private Long parseId(String idStr) {
        try {
            return idStr == null ? null : Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse ID string '{}' into Long", idStr);
            return null;
        }
    }
}
