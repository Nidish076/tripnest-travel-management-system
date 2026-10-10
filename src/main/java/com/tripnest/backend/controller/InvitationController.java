package com.tripnest.backend.controller;

import com.tripnest.backend.dto.request.InvitationRequest;
import com.tripnest.backend.dto.response.ApiResponse;
import com.tripnest.backend.dto.response.InvitationResponse;
import com.tripnest.backend.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/api/groups/{groupId}/invitations")
    public ResponseEntity<ApiResponse<InvitationResponse>> inviteUser(
            @PathVariable Long groupId,
            @Valid @RequestBody InvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invitation sent", invitationService.inviteUser(groupId, request)));
    }

    @GetMapping("/api/invitations")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations() {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getMyInvitations()));
    }

    @GetMapping("/api/groups/{groupId}/invitations")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getGroupInvitations(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(invitationService.getGroupInvitations(groupId)));
    }

    @PutMapping("/api/invitations/{invitationId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(@PathVariable Long invitationId) {
        invitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", null));
    }

    @PutMapping("/api/invitations/{invitationId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(@PathVariable Long invitationId) {
        invitationService.rejectInvitation(invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation rejected", null));
    }

    @DeleteMapping("/api/invitations/{invitationId}")
    public ResponseEntity<ApiResponse<Void>> cancelInvitation(@PathVariable Long invitationId) {
        invitationService.cancelInvitation(invitationId);
        return ResponseEntity.ok(ApiResponse.success("Invitation cancelled", null));
    }
}
