package com.tripnest.backend.repository;

import com.tripnest.backend.entity.TravelPreferences;
import com.tripnest.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPreferenceRepository extends JpaRepository<TravelPreferences, Long> {
    Optional<TravelPreferences> findByUser(User user);
    Optional<TravelPreferences> findByUserId(Long userId);
    Optional<TravelPreferences> findByUserEmail(String email);
}
