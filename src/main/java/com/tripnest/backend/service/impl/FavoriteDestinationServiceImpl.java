package com.tripnest.backend.service.impl;

import com.tripnest.backend.dto.response.DestinationResponse;
import com.tripnest.backend.dto.response.FavoriteDestinationResponse;
import com.tripnest.backend.entity.Destination;
import com.tripnest.backend.entity.FavoriteDestination;
import com.tripnest.backend.entity.User;
import com.tripnest.backend.exception.BadRequestException;
import com.tripnest.backend.exception.ResourceNotFoundException;
import com.tripnest.backend.exception.UnauthorizedException;
import com.tripnest.backend.repository.DestinationRepository;
import com.tripnest.backend.repository.FavoriteDestinationRepository;
import com.tripnest.backend.repository.UserRepository;
import com.tripnest.backend.security.SecurityUtils;
import com.tripnest.backend.service.FavoriteDestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteDestinationServiceImpl implements FavoriteDestinationService {

    private final FavoriteDestinationRepository favoriteDestinationRepository;
    private final DestinationRepository destinationRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityUtils.getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public FavoriteDestinationResponse addFavorite(Long destinationId) {
        User user = getAuthenticatedUser();
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        if (favoriteDestinationRepository.existsByUserAndDestination(user, destination)) {
            throw new BadRequestException("Destination is already in your favorites");
        }

        FavoriteDestination favorite = FavoriteDestination.builder()
                .user(user)
                .destination(destination)
                .build();

        FavoriteDestination saved = favoriteDestinationRepository.save(favorite);

        // Increment favorite counter on destination
        destination.setFavoriteCount((destination.getFavoriteCount() != null ? destination.getFavoriteCount() : 0L) + 1);
        destinationRepository.save(destination);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeFavorite(Long destinationId) {
        User user = getAuthenticatedUser();
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", "id", destinationId));

        FavoriteDestination favorite = favoriteDestinationRepository.findByUserAndDestination(user, destination)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite destination not found in your list"));

        favoriteDestinationRepository.delete(favorite);

        // Decrement favorite counter on destination
        if (destination.getFavoriteCount() != null && destination.getFavoriteCount() > 0) {
            destination.setFavoriteCount(destination.getFavoriteCount() - 1);
            destinationRepository.save(destination);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteDestinationResponse> getUserFavorites() {
        User user = getAuthenticatedUser();
        List<FavoriteDestination> favorites = favoriteDestinationRepository.findByUserOrderByCreatedAtDesc(user);

        return favorites.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long destinationId) {
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return false;
        }
        return favoriteDestinationRepository.existsByUserIdAndDestinationId(currentUserId, destinationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DestinationResponse> getMostFavoritedDestinations() {
        List<Destination> destinations = favoriteDestinationRepository.findMostFavoritedDestinations();
        Long currentUserId = SecurityUtils.getCurrentUserId().orElse(null);

        return destinations.stream()
                .map(d -> DestinationResponse.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .country(d.getCountry())
                        .city(d.getCity())
                        .description(d.getDescription())
                        .imageUrl(d.getImageUrl())
                        .bestTimeToVisit(d.getBestTimeToVisit())
                        .category(d.getCategory())
                        .isPopular(d.getIsPopular())
                        .favoriteCount(d.getFavoriteCount())
                        .isFavorited(currentUserId != null && favoriteDestinationRepository.existsByUserIdAndDestinationId(currentUserId, d.getId()))
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private FavoriteDestinationResponse mapToResponse(FavoriteDestination fav) {
        Destination d = fav.getDestination();
        DestinationResponse destResponse = DestinationResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .country(d.getCountry())
                .city(d.getCity())
                .description(d.getDescription())
                .imageUrl(d.getImageUrl())
                .bestTimeToVisit(d.getBestTimeToVisit())
                .category(d.getCategory())
                .isPopular(d.getIsPopular())
                .favoriteCount(d.getFavoriteCount())
                .isFavorited(true)
                .createdAt(d.getCreatedAt())
                .build();

        return FavoriteDestinationResponse.builder()
                .id(fav.getId())
                .userId(fav.getUser().getId())
                .destination(destResponse)
                .favoritedAt(fav.getCreatedAt())
                .build();
    }
}
