package com.tripnest.backend.repository;

import com.tripnest.backend.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByDestinationId(Long destinationId);

    List<Attraction> findByDestinationIdOrderByNameAsc(Long destinationId);
}
