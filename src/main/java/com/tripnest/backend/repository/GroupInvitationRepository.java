package com.tripnest.backend.repository;

import com.tripnest.backend.entity.GroupInvitation;
import com.tripnest.backend.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    List<GroupInvitation> findByInvitedUserId(Long userId);
    List<GroupInvitation> findByGroupId(Long groupId);
    Optional<GroupInvitation> findByGroupIdAndInvitedUserIdAndStatus(Long groupId, Long invitedUserId, InvitationStatus status);
}
