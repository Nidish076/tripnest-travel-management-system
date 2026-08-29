package com.tripnest.backend.repository;

import com.tripnest.backend.entity.AccountSettings;
import com.tripnest.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountSettingRepository extends JpaRepository<AccountSettings, Long> {
    Optional<AccountSettings> findByUser(User user);
    Optional<AccountSettings> findByUserId(Long userId);
    Optional<AccountSettings> findByUserEmail(String email);
}
