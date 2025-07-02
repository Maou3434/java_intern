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
import java.util.stream.Collectors;

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

        logger.info("Fetching paginated courses - page: {}, size: {}", page, size);

        List<Course> courses = courseService.getAllCourses(page, size);

        List<CourseDTO> courseDTOs = courses.stream()
                .map(CourseMapper::toDTO)
                .peek(dto -> logger.debug("Mapped Course to CourseDTO: {}", dto))
                .collect(Collectors.toList());

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                courseDTOs
        );
    }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<CourseDTO> getCourseById(@PathVariable Long id) {
        logger.info("Fetching course with ID: {}", id);
        Course course = courseService.getCourseById(id);
        CourseDTO dto = CourseMapper.toDTO(course);
        logger.debug("Mapped Course to CourseDTO: {}", dto);

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Ret,
                dto
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseClass<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        logger.info("Creating course with data: {}", courseDTO);
        Course course = CourseMapper.toEntity(courseDTO);
        Course created = courseService.createCourse(course);
        CourseDTO dto = CourseMapper.toDTO(created);
        logger.debug("Created course mapped to DTO: {}", dto);

        return new ResponseClass<>(
                HttpStatus.CREATED,
                Constants.Crt,
                dto
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        logger.info("Updating course with ID: {}, new data: {}", id, courseDTO);
        Course courseDetails = CourseMapper.toEntity(courseDTO);
        Course updatedCourse = courseService.updateCourse(id, courseDetails);
        
        CourseDTO dto = CourseMapper.toDTO(updatedCourse);
        logger.debug("Updated course mapped to DTO: {}", dto);

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Upd,
                dto
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseClass<CourseDTO> deleteCourse(@PathVariable Long id) {
        logger.info("Deleting course with ID: {}", id);
        Course course = courseService.getCourseById(id);
        CourseDTO dto = CourseMapper.toDTO(course);
        courseService.deleteCourse(id);
        logger.debug("Deleted course mapped to DTO: {}", dto);

        return new ResponseClass<>(
                HttpStatus.OK,
                Constants.Del,
                dto
        );
    }
}
