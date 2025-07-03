package com.example.repo;

// JPA imports
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

// Transactional annotation
import jakarta.transaction.Transactional;

// Application Entity import
import com.example.entity.User;

// Java utility imports
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for {@link User} entity.
 * <p>
 * Provides methods for user CRUD operations, existence checks, and queries involving courses.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieve all users.
     *
     * @return list of all users
     */
    @Query("SELECT u FROM User u")
    List<User> findAllUsers();

    /**
     * Find a user by their unique ID.
     *
     * @param id user ID
     * @return Optional containing the user if found
     */
    @Query("SELECT u FROM User u WHERE u.id = ?1")
    Optional<User> findUserById(Long id);

    /**
     * Delete a user by their ID.
     *
     * @param id user ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.id = ?1")
    void deleteUserById(Long id);

    /**
     * Insert a new user using a native SQL query.
     *
     * @param name  user name
     * @param email user email
     */
    @Modifying
    @Transactional
    @Query(
        value = "INSERT INTO users (name, email) VALUES (?1, ?2)", 
        nativeQuery = true
    )
    void insertUser(String name, String email);

    /**
     * Check if a user exists by their email address.
     *
     * @param email user email
     * @return true if a user exists with the email, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = ?1")
    boolean existsByEmail(String email);

    /**
     * Find users enrolled in a course by the course ID.
     *
     * @param courseId course ID
     * @return list of users enrolled in the course
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.courses c WHERE c.id = ?1")
    List<User> findUsersByCourseId(Long courseId);

    /**
     * Find users enrolled in any of the given course IDs.
     *
     * @param courseIds set of course IDs
     * @return list of users enrolled in at least one of the courses
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.courses c WHERE c.id IN ?1")
    List<User> findAllByCoursesIdIn(Set<Long> courseIds);
}
