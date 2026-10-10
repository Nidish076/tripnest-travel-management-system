package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.InvitationRequest;
import com.tripnest.backend.dto.response.InvitationResponse;

import java.util.List;

public interface InvitationService {
    InvitationResponse inviteUser(Long groupId, InvitationRequest request);
    List<InvitationResponse> getMyInvitations();
    List<InvitationResponse> getGroupInvitations(Long groupId);
    void acceptInvitation(Long invitationId);
    void rejectInvitation(Long invitationId);
    void cancelInvitation(Long invitationId);
}
