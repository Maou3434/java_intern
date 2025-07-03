package com.example.controller;

import com.example.constants.Constants;
import com.example.dto.CourseDTO;
import com.example.dto.PlatformDTO;
import com.example.dto.UserDTO;
import com.example.entity.Course;
import com.example.entity.Platform;
import com.example.mapper.PlatformMapper;
import com.example.response.ResponseClass;
import com.example.service.PlatformService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platforms")
public class PlatformController {

    private static final Logger logger = LoggerFactory.getLogger(PlatformController.class);

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    // --- SQL CRUD ---

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<List<PlatformDTO>> getAllPlatforms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Received request to get paginated platforms");

        List<Platform> platforms = platformService.getAllPlatforms(page, size);

        List<PlatformDTO> dtos = platforms.stream()
                .map(PlatformMapper::toDTO)
                .peek(dto -> logger.debug("Mapped a platform entity to DTO"))
                .collect(Collectors.toList());

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                dtos
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<PlatformDTO> getPlatformById(@PathVariable Long id) {
        logger.info("Received request to get a platform by ID");

        Platform platform = platformService.getPlatformById(id);
        PlatformDTO dto = PlatformMapper.toDTO(platform);

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                dto
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseClass<PlatformDTO> createPlatform(@Valid @RequestBody PlatformDTO platformDTO) {
        logger.info("Received request to create a new platform");

        Set<Course> courses = platformService.getCoursesByDTO(platformDTO);
        Platform platform = PlatformMapper.toEntity(platformDTO, courses);
        Platform created = platformService.createPlatform(platform);
        PlatformDTO dto = PlatformMapper.toDTO(created);

        logger.debug("Platform created successfully");

        return new ResponseClass<>(
                HttpStatus.CREATED,
                Constants.Crt,
                dto
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<PlatformDTO> updatePlatform(@PathVariable Long id, @Valid @RequestBody PlatformDTO platformDTO) {
        logger.info("Received request to update a platform");

        Set<Course> courses = platformService.getCoursesByDTO(platformDTO);
        Platform platformDetails = PlatformMapper.toEntity(platformDTO, courses);
        Platform updated = platformService.updatePlatform(id, platformDetails);
        PlatformDTO dto = PlatformMapper.toDTO(updated);

        logger.debug("Platform updated successfully");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Upd,
                dto
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<PlatformDTO> deletePlatform(@PathVariable Long id) {
        logger.info("Received request to delete a platform");

        PlatformDTO dto = platformService.deletePlatformById(id);

        logger.debug("Platform deleted successfully");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Del,
                dto
        );
    }

    // --- Mongo reads ---

    @GetMapping("/{mongoId}/users")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<List<UserDTO>> getUsersByPlatformMongoId(@PathVariable String mongoId) {
        logger.info("Received request to get users from MongoDB for a platform");

        List<UserDTO> users = platformService.getUsersByPlatformIdFromMongo(mongoId);

        logger.debug("Retrieved user list from MongoDB");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                users
        );
    }

    @GetMapping("/{mongoId}/courses")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<List<CourseDTO>> getCoursesByPlatformMongoId(@PathVariable String mongoId) {
        logger.info("Received request to get courses from MongoDB for a platform");

        List<CourseDTO> courses = platformService.getCoursesByPlatformIdFromMongo(mongoId);

        logger.debug("Retrieved course list from MongoDB");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                courses
        );
    }
}
