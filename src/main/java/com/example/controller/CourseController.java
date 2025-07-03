package com.example.controller;

import com.example.constants.Constants;
import com.example.dto.CourseDTO;
import com.example.entity.Course;
import com.example.mapper.CourseMapper;
import com.example.response.ResponseClass;
import com.example.service.CourseService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<List<CourseDTO>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Fetching paginated courses");

        List<Course> courses = courseService.getAllCourses(page, size);

        List<CourseDTO> courseDTOs = courses.stream()
                .map(course -> {
                    CourseDTO dto = CourseMapper.toDTO(course);
                    logger.debug("Mapped Course to CourseDTO: {}", dto);
                    return dto;
                })
                .toList(); 

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.RETRIEVAL,
                courseDTOs
        );
    }



    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<CourseDTO> getCourseById(@PathVariable Long id) {
        logger.info("Fetching course");

        Course course = courseService.getCourseById(id);
        CourseDTO dto = CourseMapper.toDTO(course);
        logger.debug("Mapped Course to CourseDTO");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.RETRIEVAL,
                dto
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseClass<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        logger.info("Creating course");

        Course course = CourseMapper.toEntity(courseDTO);
        Course created = courseService.createCourse(course);
        CourseDTO dto = CourseMapper.toDTO(created);
        logger.debug("Created course mapped to DTO");

        return new ResponseClass<>(
                HttpStatus.CREATED,
                Constants.CREATION,
                dto
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        logger.info("Updating course");

        Course courseDetails = CourseMapper.toEntity(courseDTO);
        Course updatedCourse = courseService.updateCourse(id, courseDetails);

        CourseDTO dto = CourseMapper.toDTO(updatedCourse);
        logger.debug("Updated course mapped to DTO");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.UPDATION,
                dto
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<CourseDTO> deleteCourse(@PathVariable Long id) {
        logger.info("Deleting course");

        Course course = courseService.getCourseById(id);
        CourseDTO dto = CourseMapper.toDTO(course);
        courseService.deleteCourse(id);
        logger.debug("Deleted course mapped to DTO");

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.DELETION,
                dto
        );
    }
}
