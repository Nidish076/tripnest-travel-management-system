package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.GroupRequest;
import com.tripnest.backend.dto.response.GroupMemberResponse;
import com.tripnest.backend.dto.response.GroupResponse;

import java.util.List;

public interface GroupService {
    GroupResponse createGroup(GroupRequest request);
    List<GroupResponse> getUserGroups();
    GroupResponse getGroupDetails(Long groupId);
    GroupResponse updateGroup(Long groupId, GroupRequest request);
    void deleteGroup(Long groupId);
    
    // Phase 2: Members
    List<GroupMemberResponse> getGroupMembers(Long groupId);
    void changeMemberRole(Long groupId, Long userId, String newRole);
    void removeMember(Long groupId, Long userId);
}
