package com.tripnest.backend.repository;

import com.tripnest.backend.entity.TravelGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelGroupRepository extends JpaRepository<TravelGroup, Long> {
}
