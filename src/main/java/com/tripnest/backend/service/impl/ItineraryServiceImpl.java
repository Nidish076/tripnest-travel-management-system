package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.ItineraryRequest;
import com.tripnest.backend.dto.ItineraryResponse;
import com.tripnest.backend.dto.UserResponse;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.model.ItineraryItem;
import com.tripnest.backend.model.Trip;
import com.tripnest.backend.model.TripMember;
import com.tripnest.backend.model.User;
import com.tripnest.backend.repository.ItineraryRepository;
import com.tripnest.backend.repository.TripMemberRepository;
import com.tripnest.backend.repository.TripRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.service.ItineraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItineraryResponse addItineraryItem(Long tripId, ItineraryRequest request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You must be a member of this trip to add itinerary items"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        ItineraryItem item = ItineraryItem.builder()
                .trip(trip)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .location(request.getLocation())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .estimatedCost(request.getEstimatedCost() != null ? request.getEstimatedCost() : 0.0)
                .createdBy(user)
                .build();

        ItineraryItem saved = itineraryRepository.save(item);
        return mapToItineraryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryResponse> getItinerary(Long tripId, Long currentUserId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        return itineraryRepository.findByTripIdOrderByDateAscStartTimeAsc(tripId).stream()
                .map(this::mapToItineraryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryResponse> getItineraryByDate(Long tripId, LocalDate date, Long currentUserId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        return itineraryRepository.findByTripIdAndDateOrderByStartTimeAsc(tripId, date).stream()
                .map(this::mapToItineraryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItineraryResponse updateItineraryItem(Long itemId, ItineraryRequest request, Long currentUserId) {
        ItineraryItem item = itineraryRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary item not found with id: " + itemId));

        TripMember member = tripMemberRepository.findByTripIdAndUserId(item.getTrip().getId(), currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        boolean isCreator = item.getCreatedBy().getId().equals(currentUserId);
        boolean isPrivileged = member.getRole() == TripMember.MemberRole.OWNER || member.getRole() == TripMember.MemberRole.ADMIN;

        if (!isCreator && !isPrivileged) {
            throw new AccessDeniedException("You do not have permission to modify this itinerary item");
        }

        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setDate(request.getDate());
        item.setStartTime(request.getStartTime());
        item.setEndTime(request.getEndTime());
        if (request.getEstimatedCost() != null) {
            item.setEstimatedCost(request.getEstimatedCost());
        }

        ItineraryItem updated = itineraryRepository.save(item);
        return mapToItineraryResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItineraryItem(Long itemId, Long currentUserId) {
        ItineraryItem item = itineraryRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary item not found with id: " + itemId));

        TripMember member = tripMemberRepository.findByTripIdAndUserId(item.getTrip().getId(), currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        boolean isCreator = item.getCreatedBy().getId().equals(currentUserId);
        boolean isPrivileged = member.getRole() == TripMember.MemberRole.OWNER || member.getRole() == TripMember.MemberRole.ADMIN;

        if (!isCreator && !isPrivileged) {
            throw new AccessDeniedException("You do not have permission to delete this itinerary item");
        }

        itineraryRepository.delete(item);
    }

    private ItineraryResponse mapToItineraryResponse(ItineraryItem item) {
        UserResponse createdBy = UserResponse.builder()
                .id(item.getCreatedBy().getId())
                .name(item.getCreatedBy().getName())
                .email(item.getCreatedBy().getEmail())
                .profileImage(item.getCreatedBy().getProfileImage())
                .build();

        return ItineraryResponse.builder()
                .id(item.getId())
                .tripId(item.getTrip().getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .date(item.getDate())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .estimatedCost(item.getEstimatedCost())
                .createdBy(createdBy)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
