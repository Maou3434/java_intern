package com.example.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.document.PlatformDocument;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PlatformDocument entity stored in MongoDB.
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
     * Find all platform documents.
     *
     * @return list of all PlatformDocuments
     */
    @Query("{}")
    List<PlatformDocument> findAll();
}
