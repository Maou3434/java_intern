package com.example.repo;

import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository interface for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find all users.
     */
    @Query("SELECT u FROM User u")
    List<User> findAllUsers();

    /**
     * Find user by id.
     *
     * @param id User id
     */
    @Query("SELECT u FROM User u WHERE u.id = ?1")
    Optional<User> findUserById(Long id);

    /**
     * Delete user by id.
     *
     * @param id User id
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.id = ?1")
    void deleteUserById(Long id);

    /**
     * Insert a new user (native query).
     *
     * @param name User name
     * @param email User email
     */
    @Modifying
    @Transactional
    @Query(
        value = "INSERT INTO users (name, email) VALUES (?1, ?2)", 
        nativeQuery = true
    )
    void insertUser(String name, String email);
    
    /**
     * Check if a user exists by email.
     *
     * @param email User email
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = ?1")
    boolean existsByEmail(String email);
    
    @Query("SELECT DISTINCT u FROM User u JOIN u.courses c WHERE c.id = ?1")
    List<User> findUsersByCourseId(Long courseId);

    @Query("SELECT DISTINCT u FROM User u JOIN u.courses c WHERE c.id IN ?1")
    List<User> findAllByCoursesIdIn(Set<Long> courseIds);	
}
