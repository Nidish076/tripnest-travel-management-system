package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.DestinationRequest;
import com.tripnest.backend.dto.response.DestinationResponse;
import com.tripnest.backend.entity.Destination;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.repository.DestinationRepository;
import com.tripnest.backend.repository.FavoriteDestinationRepository;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DestinationServiceImpl implements DestinationService {

    private final DestinationRepository destinationRepository;
    private final FavoriteDestinationRepository favoriteDestinationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DestinationResponse> searchDestinations(
            String query,
            String country,
            String category,
            Boolean isPopular,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Destination> destinations = destinationRepository.searchDestinations(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (country != null && !country.isBlank()) ? country.trim() : null,
                (category != null && !category.isBlank()) ? category.trim() : null,
                isPopular,
                pageable
        );

        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);

        return destinations.map(d -> mapToResponse(d, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public DestinationResponse getDestinationById(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", id));

        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        return mapToResponse(destination, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationResponse> getPopularDestinations() {
        List<Destination> popular = destinationRepository.findByIsPopularTrue();
        if (popular.isEmpty()) {
            popular = destinationRepository.findTop10ByOrderByFavoriteCountDesc();
        }

        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        return popular.stream()
                .map(d -> mapToResponse(d, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DestinationResponse createDestination(DestinationRequest request) {
        if (destinationRepository.findByNameIgnoreCase(request.getName().trim()).isPresent()) {
            throw new BadRequestException("A destination with name '" + request.getName() + "' already exists");
        }

        Destination destination = Destination.builder()
                .name(request.getName().trim())
                .country(request.getCountry().trim())
                .city(request.getCity())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .bestTimeToVisit(request.getBestTimeToVisit())
                .category(request.getCategory())
                .isPopular(request.getIsPopular() != null ? request.getIsPopular() : false)
                .favoriteCount(0L)
                .build();

        Destination saved = destinationRepository.save(destination);
        return mapToResponse(saved, null);
    }

    @Override
    @Transactional
    public DestinationResponse updateDestination(Long id, DestinationRequest request) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", id));

        destination.setName(request.getName().trim());
        destination.setCountry(request.getCountry().trim());
        destination.setCity(request.getCity());
        destination.setDescription(request.getDescription());
        destination.setImageUrl(request.getImageUrl());
        destination.setBestTimeToVisit(request.getBestTimeToVisit());
        destination.setCategory(request.getCategory());
        if (request.getIsPopular() != null) {
            destination.setIsPopular(request.getIsPopular());
        }

        Destination saved = destinationRepository.save(destination);
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        return mapToResponse(saved, currentUserId);
    }

    @Override
    @Transactional
    public void deleteDestination(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", id));

        destinationRepository.delete(destination);
    }

    public DestinationResponse mapToResponse(Destination destination, Long currentUserId) {
        boolean isFavorited = false;
        if (currentUserId != null) {
            isFavorited = favoriteDestinationRepository.existsByUserIdAndDestinationId(currentUserId, destination.getId());
        }

        return DestinationResponse.builder()
                .id(destination.getId())
                .name(destination.getName())
                .country(destination.getCountry())
                .city(destination.getCity())
                .description(destination.getDescription())
                .imageUrl(destination.getImageUrl())
                .bestTimeToVisit(destination.getBestTimeToVisit())
                .category(destination.getCategory())
                .isPopular(destination.getIsPopular())
                .favoriteCount(destination.getFavoriteCount() != null ? destination.getFavoriteCount() : 0L)
                .isFavorited(isFavorited)
                .createdAt(destination.getCreatedAt())
                .build();
    }
}
