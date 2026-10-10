package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.GroupRequest;
import com.tripnest.backend.dto.response.GroupMemberResponse;
import com.tripnest.backend.dto.response.GroupResponse;
import com.tripnest.backend.entity.GroupMember;
import com.tripnest.backend.entity.GroupRole;
import com.tripnest.backend.entity.TravelGroup;
import com.tripnest.backend.entity.User;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.GroupMemberRepository;
import com.tripnest.backend.repository.TravelGroupRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.security.GroupAuthorizationService;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final TravelGroupRepository travelGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupAuthorizationService groupAuthorizationService;

    private User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        User currentUser = getCurrentUser();

        TravelGroup group = TravelGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        group = travelGroupRepository.save(group);

        GroupMember adminMember = GroupMember.builder()
                .group(group)
                .user(currentUser)
                .role(GroupRole.GROUP_ADMIN)
                .build();
        groupMemberRepository.save(adminMember);

        return mapToGroupResponse(group, currentUser.getId(), 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups() {
        Long currentUserId = getCurrentUser().getId();
        return groupMemberRepository.findByUserId(currentUserId).stream()
                .map(member -> {
                    TravelGroup group = member.getGroup();
                    int memberCount = groupMemberRepository.findByGroupId(group.getId()).size();
                    return mapToGroupResponse(group, currentUserId, memberCount);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupDetails(Long groupId) {
        Long currentUserId = getCurrentUser().getId();
        groupAuthorizationService.requireMember(groupId, currentUserId);

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        int memberCount = groupMemberRepository.findByGroupId(groupId).size();
        return mapToGroupResponse(group, currentUserId, memberCount);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(Long groupId, GroupRequest request) {
        Long currentUserId = getCurrentUser().getId();
        groupAuthorizationService.requireGroupAdmin(groupId, currentUserId);

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group = travelGroupRepository.save(group);

        int memberCount = groupMemberRepository.findByGroupId(groupId).size();
        return mapToGroupResponse(group, currentUserId, memberCount);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        Long currentUserId = getCurrentUser().getId();
        groupAuthorizationService.requireGroupAdmin(groupId, currentUserId);

        TravelGroup group = travelGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        // Delete all members
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        groupMemberRepository.deleteAll(members);

        // We will need to handle trips, invitations, and expenses deletion here later

        travelGroupRepository.delete(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        Long currentUserId = getCurrentUser().getId();
        groupAuthorizationService.requireMember(groupId, currentUserId);

        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(this::mapToMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void changeMemberRole(Long groupId, Long userId, String newRoleStr) {
        Long currentUserId = getCurrentUser().getId();
        groupAuthorizationService.requireGroupAdmin(groupId, currentUserId);

        GroupRole newRole;
        try {
            newRole = GroupRole.valueOf(newRoleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role");
        }

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in group"));

        if (member.getRole() == GroupRole.GROUP_ADMIN && newRole == GroupRole.MEMBER) {
            int adminCount = groupMemberRepository.countByGroupIdAndRole(groupId, GroupRole.GROUP_ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot change role of the last group administrator");
            }
        }

        member.setRole(newRole);
        groupMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId) {
        Long currentUserId = getCurrentUser().getId();
        groupAuthorizationService.requireGroupAdmin(groupId, currentUserId);

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in group"));

        if (member.getRole() == GroupRole.GROUP_ADMIN) {
            int adminCount = groupMemberRepository.countByGroupIdAndRole(groupId, GroupRole.GROUP_ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot remove the last group administrator");
            }
        }

        groupMemberRepository.delete(member);
    }

    private GroupResponse mapToGroupResponse(TravelGroup group, Long currentUserId, int memberCount) {
        String currentUserRole = groupMemberRepository.findByGroupIdAndUserId(group.getId(), currentUserId)
                .map(m -> m.getRole().name())
                .orElse(null);

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdById(group.getCreatedBy().getId())
                .createdByName(group.getCreatedBy().getName())
                .createdAt(group.getCreatedAt())
                .memberCount(memberCount)
                .currentUserRole(currentUserRole)
                .build();
    }

    private GroupMemberResponse mapToMemberResponse(GroupMember member) {
        return GroupMemberResponse.builder()
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
