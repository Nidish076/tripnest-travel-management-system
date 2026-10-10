package com.tripnest.backend.controller;

import com.tripnest.backend.dto.request.GroupRequest;
import com.tripnest.backend.dto.response.ApiResponse;
import com.tripnest.backend.dto.response.GroupMemberResponse;
import com.tripnest.backend.dto.response.GroupResponse;
import com.tripnest.backend.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", groupService.createGroup(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getUserGroups() {
        return ResponseEntity.ok(ApiResponse.success(groupService.getUserGroups()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupDetails(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupDetails(groupId)));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody GroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", groupService.updateGroup(groupId, request)));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success("Group deleted successfully", null));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getGroupMembers(@PathVariable Long groupId) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getGroupMembers(groupId)));
    }

    @PutMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> changeMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam String role) {
        groupService.changeMemberRole(groupId, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Member role updated successfully", null));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
