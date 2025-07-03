package com.example.repo;

// Entity import
import com.example.entity.Course;

// Spring Data JPA imports
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// Transaction management
import jakarta.transaction.Transactional;

// Java Collections and Optional
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for {@link Course} entity.
 * <p>
 * Provides CRUD and custom query methods for Course persistence.
 * </p>
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Retrieve all courses.
     *
     * @return list of all courses
     */
    @Query("SELECT c FROM Course c")
    List<Course> findAll();

    /**
     * Find a course by its ID.
     *
     * @param id the course ID
     * @return an Optional containing the found course or empty if not found
     */
    @Query("SELECT c FROM Course c WHERE c.id = ?1")
    Optional<Course> findById(Long id);

    /**
     * Find all courses by a set of IDs.
     *
     * @param ids set of course IDs
     * @return list of matching courses
     */
    @Query("SELECT c FROM Course c WHERE c.id IN ?1")
    List<Course> findAllById(Set<Long> ids);

    /**
     * Delete a course by its ID.
     *
     * @param id the course ID to delete
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Course c WHERE c.id = ?1")
    void deleteById(Long id);

    /**
     * Check if a course exists by ID.
     *
     * @param id the course ID
     * @return true if a course with the given ID exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE c.id = ?1")
    boolean existsById(Long id);

    /**
     * Check if a course exists by title.
     *
     * @param title the course title
     * @return true if a course with the given title exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE c.title = ?1")
    boolean existsByTitle(String title);
}
