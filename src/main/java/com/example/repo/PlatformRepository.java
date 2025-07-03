package com.example.repo;

// Spring Data JPA imports
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Application Entity import
import com.example.entity.Platform;

// Java Optional
import java.util.Optional;

/**
 * Repository interface for {@link Platform} entity in SQL database.
 * <p>
 * Primarily used for write operations and fetching platforms by name.
 * </p>
 */
@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {

    /**
     * Finds a platform by its unique name.
     * Useful for existence checks or pre-update retrieval.
     *
     * @param name the platform name
     * @return Optional containing the Platform if present
     */
    Optional<Platform> findByName(String name);
}
