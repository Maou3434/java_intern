package com.example.controller;

// Java standard library imports
import java.util.List;
import java.util.Set;

// Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Spring Framework imports
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Project-specific imports
import com.example.constants.Constants;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.response.ResponseClass;
import com.example.service.UserService;

// Jakarta Validation imports
import jakarta.validation.Valid;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get paginated list of users.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Received request to get paginated users");

        List<User> users = userService.getAllUsers(page, size);

        List<UserDTO> userDTOs = users.stream()
                .map(user -> {
                    UserDTO dto = UserMapper.toDTO(user);
                    logger.debug("Mapped user entity to DTO: {}", dto);
                    return dto;
                })
                .toList(); // Java 16+ (returns unmodifiable list)

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.RETRIEVAL,
                userDTOs
        );
    }

    /**
     * Get a user by ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("Received request to get a user by ID");

        User user = userService.getUserById(id);

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.RETRIEVAL,
                UserMapper.toDTO(user)
        );
    }

    /**
     * Create a new user.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseClass<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        logger.info("Received request to create a new user");

        User user = UserMapper.toEntity(userDTO, Set.of());
        User created = userService.createUser(user);

        logger.debug("User created successfully");

        return new ResponseClass<>(
                HttpStatus.CREATED,
                Constants.CREATION,
                UserMapper.toDTO(created)
        );
    }

    /**
     * Update an existing user by ID.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        logger.info("Received request to update a user");

        User existingUser = userService.getUserById(id);
        User userDetails = UserMapper.toEntity(userDTO, existingUser.getCourses());
        User updatedUser = userService.updateUser(id, userDetails);

        logger.debug("User updated successfully");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.UPDATION,
                UserMapper.toDTO(updatedUser)
        );
    }

    /**
     * Delete a user by ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> deleteUser(@PathVariable Long id) {
        logger.info("Received request to delete a user");

        UserDTO deletedUser = userService.deleteUser(id);

        logger.debug("User deleted successfully");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.DELETION,
                deletedUser
        );
    }

    /**
     * Enroll a user in multiple courses.
     */
    @PostMapping("/{id}/courses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> enrollUserInCourses(@PathVariable Long id, @RequestBody Set<Long> courseIds) {
        logger.info("Received request to enroll user in courses");

        User updatedUser = userService.enrollUserInCourses(id, courseIds);

        logger.debug("User enrolled in {} courses", courseIds.size());

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.ENROLLEMENT,
                UserMapper.toDTO(updatedUser)
        );
    }
}
