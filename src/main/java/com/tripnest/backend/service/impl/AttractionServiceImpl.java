package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.request.AttractionRequest;
import com.tripnest.backend.dto.response.AttractionResponse;
import com.tripnest.backend.entity.Attraction;
import com.tripnest.backend.entity.Destination;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.repository.AttractionRepository;
import com.tripnest.backend.repository.DestinationRepository;
import com.tripnest.backend.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;
    private final DestinationRepository destinationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AttractionResponse> getAttractionsByDestinationId(Long destinationId) {
        if (!destinationRepository.existsById(destinationId)) {
            throw new ResourceNotFoundException("Destination", "id", destinationId);
        }

        List<Attraction> attractions = attractionRepository.findByDestinationIdOrderByNameAsc(destinationId);
        return attractions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttractionResponse getAttractionById(Long attractionId) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attractionId));
        return mapToResponse(attraction);
    }

    @Override
    @Transactional
    public AttractionResponse addAttraction(Long destinationId, AttractionRequest request) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        Attraction attraction = Attraction.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .category(request.getCategory())
                .entryFee(request.getEntryFee())
                .destination(destination)
                .build();

        destination.addAttraction(attraction);
        Attraction saved = attractionRepository.save(attraction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AttractionResponse updateAttraction(Long attractionId, AttractionRequest request) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attractionId));

        if (request.getName() != null && !request.getName().isBlank()) {
            attraction.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            attraction.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            attraction.setImageUrl(request.getImageUrl());
        }
        if (request.getLocation() != null) {
            attraction.setLocation(request.getLocation());
        }
        if (request.getCategory() != null) {
            attraction.setCategory(request.getCategory());
        }
        if (request.getEntryFee() != null) {
            attraction.setEntryFee(request.getEntryFee());
        }

        Attraction saved = attractionRepository.save(attraction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAttraction(Long attractionId) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attractionId));

        Destination destination = attraction.getDestination();
        if (destination != null) {
            destination.removeAttraction(attraction);
        }
        attractionRepository.delete(attraction);
    }

    private AttractionResponse mapToResponse(Attraction attraction) {
        return AttractionResponse.builder()
                .id(attraction.getId())
                .destinationId(attraction.getDestination() != null ? attraction.getDestination().getId() : null)
                .name(attraction.getName())
                .description(attraction.getDescription())
                .imageUrl(attraction.getImageUrl())
                .location(attraction.getLocation())
                .category(attraction.getCategory())
                .entryFee(attraction.getEntryFee())
                .createdAt(attraction.getCreatedAt())
                .updatedAt(attraction.getUpdatedAt())
                .build();
    }
}
