package com.tripnest.backend.service;

import com.tripnest.backend.dto.response.AdminUserDetailResponse;
import com.tripnest.backend.dto.response.DestinationResponse;
import com.tripnest.backend.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminService {
    Page<UserSummaryResponse> searchUsers(String query, Boolean isActive, String role, int page, int size, String sortBy, String sortDir);
    AdminUserDetailResponse getUserDetail(Long id);
    UserSummaryResponse updateUserStatus(Long id, Boolean isActive);
    void deleteUser(Long id);
    List<DestinationResponse> getPopularDestinationsAnalytics();
}
