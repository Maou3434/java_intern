package com.example.repo;

// Spring Data MongoDB imports
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

// Application Document import
import com.example.document.PlatformDocument;

// Java Collections and Optional
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link PlatformDocument} entity in MongoDB.
 * <p>
 * Provides methods to perform CRUD operations and custom queries on Platform documents.
 * </p>
 */
@Repository
public interface PlatformDocRepository extends MongoRepository<PlatformDocument, String> {

    /**
     * Find a platform document by its name.
     *
     * @param name the platform name
     * @return an Optional containing the PlatformDocument if found
     */
    @Query("{ 'name': ?0 }")
    Optional<PlatformDocument> findByName(String name);

    /**
     * Retrieve all platform documents.
     *
     * @return list of all PlatformDocuments
     */
    @Query("{}")
    List<PlatformDocument> findAll();
}
