package com.tripnest.backend.service.impl;

import com.tripnest.backend.entity.Itinerary;
import com.tripnest.backend.entity.TravelGroup;
import com.tripnest.backend.entity.Trip;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.ItineraryRepository;
import com.tripnest.backend.repository.TravelGroupRepository;
import com.tripnest.backend.repository.TripRepository;
import com.tripnest.backend.security.GroupAuthorizationService;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.GroupTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupTripServiceImpl implements GroupTripService {

    private final TripRepository tripRepository;
    private final TravelGroupRepository travelGroupRepository;
    private final ItineraryRepository itineraryRepository;
    private final GroupAuthorizationService authorizationService;

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    @Override
    @Transactional
    public void shareTrip(Long tripId, Long groupId) {
        Long currentUserId = getCurrentUserId();
        
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getUser().getId().equals(currentUserId)) {
            // Must be trip owner to share
            throw new UnauthorizedException("Only the trip owner can share this trip");
        }

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        authorizationService.requireMember(groupId, currentUserId);

        trip.setGroup(group);
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void unshareTrip(Long tripId) {
        Long currentUserId = getCurrentUserId();
        
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getGroup() == null) {
            throw new BadRequestException("Trip is not shared with any group");
        }

        // Allow owner or group admin to unshare
        boolean isOwner = trip.getUser().getId().equals(currentUserId);
        boolean isGroupAdmin = authorizationService.isGroupAdmin(trip.getGroup().getId(), currentUserId);

        if (!isOwner && !isGroupAdmin) {
            throw new UnauthorizedException("Only the trip owner or a group admin can unshare this trip");
        }

        trip.setGroup(null);
        tripRepository.save(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trip> getGroupTrips(Long groupId) {
        authorizationService.requireMember(groupId, getCurrentUserId());
        return tripRepository.findByGroupId(groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Itinerary> getGroupTripItineraries(Long groupId, Long tripId) {
        authorizationService.requireMember(groupId, getCurrentUserId());
        
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
                
        if (trip.getGroup() == null || !trip.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("This trip is not shared with this group");
        }

        return itineraryRepository.findByTripId(tripId);
    }
}
