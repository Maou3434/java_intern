package com.example.mapper;

import com.example.entity.User;
import com.example.entity.Course;
import com.example.dto.UserDTO;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between User entity and UserDTO.
 */
public class UserMapper {
	
	private UserMapper() {
		throw new IllegalStateException("This is a utility class");
	}

    /**
     * Converts User entity to UserDTO.
     *
     * @param user
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
     * Converts UserDTO to User entity.
     *
     * @param dto
     * @param courses Set of courses associated with the user
     */
    public static User toEntity(UserDTO dto, Set<Course> courses) {
        User user = new User();
        // Do NOT set ID for new entities
        if (dto.getId() != null) {
            user.setId(dto.getId());
        }
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setCourses(courses);
        return user;
    }

}
