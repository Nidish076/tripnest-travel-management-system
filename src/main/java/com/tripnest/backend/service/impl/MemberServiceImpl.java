package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.JoinTripRequest;
import com.tripnest.backend.dto.TripMemberResponse;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.model.Trip;
import com.tripnest.backend.model.TripMember;
import com.tripnest.backend.model.User;
import com.tripnest.backend.repository.TripMemberRepository;
import com.tripnest.backend.repository.TripRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TripMemberResponse joinTrip(Long tripId, JoinTripRequest request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        if (!trip.getInviteCode().equalsIgnoreCase(request.getInviteCode().trim())) {
            throw new BadRequestException("Invalid invite code for this trip");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        if (tripMemberRepository.existsByTripIdAndUserId(tripId, currentUserId)) {
            throw new BadRequestException("User is already a member of this trip");
        }

        TripMember newMember = TripMember.builder()
                .trip(trip)
                .user(user)
                .role(TripMember.MemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();

        TripMember saved = tripMemberRepository.save(newMember);

        return mapToMemberResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripMemberResponse> getTripMembers(Long tripId, Long currentUserId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        return tripMemberRepository.findByTripId(tripId).stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(Long tripId, Long targetUserId, Long currentUserId) {
        tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));

        TripMember targetMember = tripMemberRepository.findByTripIdAndUserId(tripId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this trip"));

        TripMember callerMember = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this trip"));

        // If user is removing themselves
        if (currentUserId.equals(targetUserId)) {
            if (targetMember.getRole() == TripMember.MemberRole.OWNER) {
                throw new BadRequestException("Trip OWNER cannot leave the trip. Delete the trip instead.");
            }
            tripMemberRepository.delete(targetMember);
            return;
        }

        // Caller is removing someone else
        if (callerMember.getRole() == TripMember.MemberRole.MEMBER) {
            throw new AccessDeniedException("Only OWNER or ADMIN can remove other members");
        }

        if (targetMember.getRole() == TripMember.MemberRole.OWNER) {
            throw new BadRequestException("The trip OWNER cannot be removed from the trip");
        }

        if (callerMember.getRole() == TripMember.MemberRole.ADMIN && targetMember.getRole() == TripMember.MemberRole.ADMIN) {
            throw new AccessDeniedException("ADMIN cannot remove another ADMIN");
        }

        tripMemberRepository.delete(targetMember);
    }

    private TripMemberResponse mapToMemberResponse(TripMember member) {
        return TripMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .profileImage(member.getUser().getProfileImage())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
