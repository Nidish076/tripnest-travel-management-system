package com.tripnest.backend.service;

import com.tripnest.backend.dto.request.DestinationRequest;
import com.tripnest.backend.dto.response.DestinationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DestinationService {
    Page<DestinationResponse> searchDestinations(
            String query,
            String country,
            String category,
            Boolean isPopular,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    DestinationResponse getDestinationById(Long id);

    List<DestinationResponse> getPopularDestinations();

    DestinationResponse createDestination(DestinationRequest request);

    DestinationResponse updateDestination(Long id, DestinationRequest request);

    void deleteDestination(Long id);
}
