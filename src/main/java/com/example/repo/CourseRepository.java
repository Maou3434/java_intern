package com.example.repo;

import com.example.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for Course entity.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Find all courses.
     */
    @Query("SELECT c FROM Course c")
    List<Course> findAll();

    /**
     * Find course by id.
     *
     * @param id Course id
     */
    @Query("SELECT c FROM Course c WHERE c.id = ?1")
    Optional<Course> findById(Long id);

    /**
     * Find all courses by a set of ids.
     *
     * @param ids Set of course ids
     */
    @Query("SELECT c FROM Course c WHERE c.id IN ?1")
    List<Course> findAllById(Set<Long> ids);

    /**
     * Delete course by id.
     *
     * @param id Course id
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Course c WHERE c.id = ?1")
    void deleteById(Long id);

    /**
     * Check if a course exists by id.
     *
     * @param id Course id
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE c.id = ?1")
    boolean existsById(Long id);
    
    /**
     * Check if a course exists by title.
     *
     * @param title Course title
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE c.title = ?1")
    boolean existsByTitle(String title);
}
