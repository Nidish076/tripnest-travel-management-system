package com.tripnest.backend.security;

import com.tripnest.backend.entity.GroupRole;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupAuthorizationService {

    private final GroupMemberRepository groupMemberRepository;

    public boolean isMember(Long groupId, Long userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    public boolean isGroupAdmin(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getRole() == GroupRole.GROUP_ADMIN)
                .orElse(false);
    }

    public void requireMember(Long groupId, Long userId) {
        if (!isMember(groupId, userId)) {
            throw new UnauthorizedException("You are not a member of this travel group");
        }
    }

    public void requireGroupAdmin(Long groupId, Long userId) {
        if (!isGroupAdmin(groupId, userId)) {
            throw new UnauthorizedException("Only group administrators can perform this action");
        }
    }
}
