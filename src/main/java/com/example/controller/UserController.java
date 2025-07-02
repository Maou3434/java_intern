package com.example.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.constants.Constants;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.response.ResponseClass;
import com.example.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Fetching paginated users - page: {}, size: {}", page, size);

        List<User> users = userService.getAllUsers(page, size);

        List<UserDTO> userDTOs = users.stream()
                .map(UserMapper::toDTO)
                .peek(dto -> logger.debug("Mapped User to UserDTO: {}", dto))
                .collect(Collectors.toList());

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                userDTOs
        );
    }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{} - Fetching user by ID", id);
        User user = userService.getUserById(id);
        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                UserMapper.toDTO(user)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseClass<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        logger.info("POST /api/users - Creating user with name: {}", userDTO.getName());
        User user = UserMapper.toEntity(userDTO, Set.of());
        User created = userService.createUser(user);
        logger.debug("Created user with ID: {}", created.getId());

        return new ResponseClass<>(
                HttpStatus.CREATED,
                Constants.Crt,
                UserMapper.toDTO(created)
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        logger.info("PUT /api/users/{} - Updating user", id);
        User existingUser = userService.getUserById(id);
        User userDetails = UserMapper.toEntity(userDTO, existingUser.getCourses());
        User updatedUser = userService.updateUser(id, userDetails);
        logger.debug("Updated user ID: {}", updatedUser.getId());

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Upd,
                UserMapper.toDTO(updatedUser)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - Deleting user", id);
        UserDTO deletedUser = userService.deleteUser(id);
        logger.debug("Deleted user with ID: {}", id);

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Del,
                deletedUser
        );
    }

    @PostMapping("/{id}/courses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<UserDTO> enrollUserInCourses(@PathVariable Long id, @RequestBody Set<Long> courseIds) {
        logger.info("POST /api/users/{}/courses - Enrolling user in courses: {}", id, courseIds);
        User updatedUser = userService.enrollUserInCourses(id, courseIds);
        logger.debug("User ID {} enrolled in {} courses", id, courseIds.size());

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Enr,
                UserMapper.toDTO(updatedUser)
        );
    }
}
