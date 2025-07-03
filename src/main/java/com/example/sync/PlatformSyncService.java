package com.example.sync;

import com.example.document.PlatformDocument;
import com.example.document.PlatformDocument.CourseEmbed;
import com.example.document.PlatformDocument.UserEmbed;
import com.example.entity.Course;
import com.example.entity.Platform;
import com.example.entity.User;
import com.example.repo.PlatformDocRepository;
import com.example.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlatformSyncService {

    private final PlatformDocRepository platformDocRepository;
    private final UserRepository userRepository;

    public PlatformSyncService(PlatformDocRepository platformDocRepository,
                               UserRepository userRepository) {
        this.platformDocRepository = platformDocRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void syncToMongo(Platform platform) {
        if (platform == null) return;

        // Collect all course IDs at once
        Set<Long> courseIds = platform.getCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toSet());

        // Fetch all users linked to these courses in a single query
        List<User> users = userRepository.findAllByCoursesIdIn(courseIds);

        // Map course ID to a list of UserEmbed for quick lookup
        Map<Long, List<UserEmbed>> usersByCourseId = new HashMap<>();
        for (User user : users) {
            for (Course course : user.getCourses()) {
                Long cId = course.getId();
                if (courseIds.contains(cId)) {
                    usersByCourseId
                        .computeIfAbsent(cId, k -> new ArrayList<>())
                        .add(new UserEmbed(
                            user.getId().toString(),
                            user.getName(),
                            user.getEmail()
                        ));
                }
            }
        }

        // Build course embeds with user embeds attached
        List<CourseEmbed> courseEmbeds = platform.getCourses().stream()
                .map(course -> new CourseEmbed(
                        course.getId().toString(),
                        course.getTitle(),
                        usersByCourseId.getOrDefault(course.getId(), Collections.emptyList())
                ))
                .toList();

        PlatformDocument doc = new PlatformDocument(
                platform.getName(),
                courseEmbeds
        );

        doc.setId(String.valueOf(platform.getId()));
        platformDocRepository.save(doc);
    }


    @Transactional
    public void deletePlatformFromMongo(Long platformId) {
        if (platformId == null) return;
        platformDocRepository.findById(String.valueOf(platformId))
                .ifPresent(platformDocRepository::delete);
    }

    @Transactional
    public void syncAllAffectedPlatforms(User user) {
        if (user == null || user.getCourses() == null) return;

        Set<Platform> platforms = user.getCourses().stream()
                .map(Course::getPlatform)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        
        platforms.forEach(this::syncToMongo);
    }

    @Transactional
    public void syncPlatformsByCourses(Collection<Course> courses) {
        if (courses == null) return;

        Set<Platform> platforms = courses.stream()
                .map(Course::getPlatform)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        platforms.forEach(this::syncToMongo);
    }
}
