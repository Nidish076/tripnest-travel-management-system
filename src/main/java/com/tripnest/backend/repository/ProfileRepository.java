package com.tripnest.backend.repository;

import com.tripnest.backend.entity.Profile;
import com.tripnest.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
    Optional<Profile> findByUserId(Long userId);
    Optional<Profile> findByUserEmail(String email);
}
