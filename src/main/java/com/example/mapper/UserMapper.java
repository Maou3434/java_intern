package com.example.mapper;

// Entity imports
import com.example.entity.User;
import com.example.entity.Course;

// DTO imports
import com.example.dto.UserDTO;

// Java Collections & Stream API
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between {@link User} entity and {@link UserDTO}.
 * <p>
 * Provides static methods to convert User objects between entity and DTO layers.
 * </p>
 */
public class UserMapper {

    // Private constructor to prevent instantiation
    private UserMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a {@link User} entity to a {@link UserDTO}.
     *
     * @param user the User entity to convert
     * @return the corresponding UserDTO or null if input is null
     */
    public static UserDTO toDTO(User user) {
        if (user == null) return null;

        Set<Long> courseIds = null;
        if (user.getCourses() != null) {
            courseIds = user.getCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toSet());
        }

        return new UserDTO(user.getId(), user.getName(), user.getEmail(), courseIds);
    }

    /**
     * Converts a {@link UserDTO} to a {@link User} entity.
     * <p>
     * Note: ID is set only if present in DTO. Typically, for new entities, ID should not be set.
     * </p>
     *
     * @param dto     the UserDTO to convert
     * @param courses the set of courses associated with the User
     * @return the corresponding User entity or null if dto is null
     */
    public static User toEntity(UserDTO dto, Set<Course> courses) {
        if (dto == null) return null;

        User user = new User();
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCourses(courses);

        return user;
    }
}
