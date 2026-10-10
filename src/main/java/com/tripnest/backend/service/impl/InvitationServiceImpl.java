package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.InvitationRequest;
import com.tripnest.backend.dto.response.InvitationResponse;
import com.tripnest.backend.entity.*;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.GroupInvitationRepository;
import com.tripnest.backend.repository.GroupMemberRepository;
import com.tripnest.backend.repository.TravelGroupRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.security.GroupAuthorizationService;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final GroupInvitationRepository invitationRepository;
    private final TravelGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupAuthorizationService authorizationService;

    private Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    @Override
    @Transactional
    public InvitationResponse inviteUser(Long groupId, InvitationRequest request) {
        Long currentUserId = getCurrentUserId();
        authorizationService.requireGroupAdmin(groupId, currentUserId);

        TravelGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (invitedUser.getId().equals(currentUserId)) {
            throw new BadRequestException("You cannot invite yourself");
        }

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, invitedUser.getId())) {
            throw new BadRequestException("User is already a member of this group");
        }

        if (invitationRepository.findByGroupIdAndInvitedUserIdAndStatus(groupId, invitedUser.getId(), InvitationStatus.PENDING).isPresent()) {
            throw new BadRequestException("An invitation is already pending for this user");
        }

        User invitedBy = userRepository.findById(currentUserId).orElseThrow();

        GroupInvitation invitation = GroupInvitation.builder()
                .group(group)
                .invitedUser(invitedUser)
                .invitedBy(invitedBy)
                .status(InvitationStatus.PENDING)
                .build();

        invitation = invitationRepository.save(invitation);
        return mapToResponse(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getMyInvitations() {
        return invitationRepository.findByInvitedUserId(getCurrentUserId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getGroupInvitations(Long groupId) {
        authorizationService.requireGroupAdmin(groupId, getCurrentUserId());
        return invitationRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptInvitation(Long invitationId) {
        Long currentUserId = getCurrentUserId();
        GroupInvitation invitation = getPendingInvitation(invitationId, currentUserId);

        if (groupMemberRepository.existsByGroupIdAndUserId(invitation.getGroup().getId(), currentUserId)) {
            throw new BadRequestException("You are already a member of this group");
        }

        GroupMember newMember = GroupMember.builder()
                .group(invitation.getGroup())
                .user(invitation.getInvitedUser())
                .role(GroupRole.MEMBER)
                .build();
        groupMemberRepository.save(newMember);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void rejectInvitation(Long invitationId) {
        Long currentUserId = getCurrentUserId();
        GroupInvitation invitation = getPendingInvitation(invitationId, currentUserId);

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void cancelInvitation(Long invitationId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        
        Long currentUserId = getCurrentUserId();
        // Allow invited user OR group admin to cancel
        boolean isInvitee = invitation.getInvitedUser().getId().equals(currentUserId);
        boolean isAdmin = authorizationService.isGroupAdmin(invitation.getGroup().getId(), currentUserId);
        
        if (!isInvitee && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to cancel this invitation");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Only pending invitations can be cancelled");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    private GroupInvitation getPendingInvitation(Long invitationId, Long userId) {
        GroupInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only respond to your own invitations");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation is already processed");
        }

        return invitation;
    }

    private InvitationResponse mapToResponse(GroupInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .groupId(invitation.getGroup().getId())
                .groupName(invitation.getGroup().getName())
                .invitedUserId(invitation.getInvitedUser().getId())
                .invitedUserName(invitation.getInvitedUser().getName())
                .invitedById(invitation.getInvitedBy().getId())
                .invitedByName(invitation.getInvitedBy().getName())
                .status(invitation.getStatus().name())
                .createdAt(invitation.getCreatedAt())
                .respondedAt(invitation.getRespondedAt())
                .build();
    }
}
