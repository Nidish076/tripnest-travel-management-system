package com.tripnest.backend.repository;

import com.tripnest.backend.entity.Destination;
import com.tripnest.backend.entity.FavoriteDestination;
import com.tripnest.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteDestinationRepository extends JpaRepository<FavoriteDestination, Long> {

    List<FavoriteDestination> findByUserOrderByCreatedAtDesc(User user);

    List<FavoriteDestination> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FavoriteDestination> findByUserAndDestination(User user, Destination destination);

    Optional<FavoriteDestination> findByUserIdAndDestinationId(Long userId, Long destinationId);

    boolean existsByUserAndDestination(User user, Destination destination);

    boolean existsByUserIdAndDestinationId(Long userId, Long destinationId);

    void deleteByUserAndDestination(User user, Destination destination);

    void deleteByUserIdAndDestinationId(Long userId, Long destinationId);

    Long countByDestination(Destination destination);

    Long countByDestinationId(Long destinationId);

    @Query("SELECT fd.destination FROM FavoriteDestination fd GROUP BY fd.destination ORDER BY COUNT(fd) DESC")
    List<Destination> findMostFavoritedDestinations();
}
