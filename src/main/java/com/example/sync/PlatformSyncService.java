package com.example.sync;

// Entities
import com.example.entity.Course;
import com.example.entity.Platform;
import com.example.entity.User;
// Document classes for MongoDB
import com.example.document.PlatformDocument;
import com.example.document.PlatformDocument.CourseEmbed;
import com.example.document.PlatformDocument.UserEmbed;
// Repositories
import com.example.repo.PlatformDocRepository;
import com.example.repo.UserRepository;

// Transaction management
import jakarta.transaction.Transactional;

// Spring stereotype
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for synchronizing Platform data
 * between relational database and MongoDB documents.
 */
@Service
public class PlatformSyncService {

    private final PlatformDocRepository platformDocRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a PlatformSyncService.
     *
     * @param platformDocRepository repository for platform documents in MongoDB
     * @param userRepository repository for User entities
     */
    public PlatformSyncService(PlatformDocRepository platformDocRepository,
                               UserRepository userRepository) {
        this.platformDocRepository = platformDocRepository;
        this.userRepository = userRepository;
    }

    /**
     * Synchronizes a Platform entity and its related data to MongoDB.
     * Embeds courses and enrolled users into a PlatformDocument.
     *
     * @param platform platform entity to sync
     */
    @Transactional
    public void syncToMongo(Platform platform) {
        if (platform == null) return;

        // Collect all course IDs for the platform
        Set<Long> courseIds = platform.getCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toSet());

        // Fetch all users enrolled in these courses with a single query
        List<User> users = userRepository.findAllByCoursesIdIn(courseIds);

        // Map each course ID to a list of UserEmbed objects for enrolled users
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

        // Create embedded course objects with their respective enrolled users
        List<CourseEmbed> courseEmbeds = platform.getCourses().stream()
                .map(course -> new CourseEmbed(
                        course.getId().toString(),
                        course.getTitle(),
                        usersByCourseId.getOrDefault(course.getId(), Collections.emptyList())
                ))
                .toList();

        // Construct and save the platform document with embedded data
        PlatformDocument doc = new PlatformDocument(
                platform.getName(),
                courseEmbeds
        );

        doc.setId(String.valueOf(platform.getId()));
        platformDocRepository.save(doc);
    }

    /**
     * Deletes a Platform document from MongoDB by platform ID.
     *
     * @param platformId ID of the platform to delete from MongoDB
     */
    @Transactional
    public void deletePlatformFromMongo(Long platformId) {
        if (platformId == null) return;
        platformDocRepository.findById(String.valueOf(platformId))
                .ifPresent(platformDocRepository::delete);
    }

    /**
     * Synchronizes all platforms affected by changes to a given user.
     * Finds platforms linked through the user's enrolled courses.
     *
     * @param user user whose affected platforms need syncing
     */
    @Transactional
    public void syncAllAffectedPlatforms(User user) {
        if (user == null || user.getCourses() == null) return;

        Set<Platform> platforms = user.getCourses().stream()
                .map(Course::getPlatform)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        platforms.forEach(this::syncToMongo);
    }

    /**
     * Synchronizes all platforms affected by a collection of courses.
     * Useful for bulk updates affecting multiple courses/platforms.
     *
     * @param courses collection of courses to find related platforms to sync
     */
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
