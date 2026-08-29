package com.tripnest.backend.repository;

import com.tripnest.backend.entity.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findByNameIgnoreCase(String name);

    List<Destination> findByIsPopularTrue();

    List<Destination> findTop10ByOrderByFavoriteCountDesc();

    @Query("SELECT d FROM Destination d WHERE " +
           "(:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.city) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.country) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:country IS NULL OR LOWER(d.country) = LOWER(:country)) AND " +
           "(:category IS NULL OR LOWER(d.category) = LOWER(:category)) AND " +
           "(:isPopular IS NULL OR d.isPopular = :isPopular)")
    Page<Destination> searchDestinations(
        @Param("query") String query,
        @Param("country") String country,
        @Param("category") String category,
        @Param("isPopular") Boolean isPopular,
        Pageable pageable
    );
}