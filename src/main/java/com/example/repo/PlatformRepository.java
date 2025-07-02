package com.example.repo;

import com.example.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Platform entity stored in SQL.
 * Mainly used for write operations.
 */
@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {

    /**
     * Find a platform by its name.
     * Useful to check existence or fetch before update.
     *
     * @param name platform name
     * @return Optional containing Platform if found
     */
    Optional<Platform> findByName(String name);
}
