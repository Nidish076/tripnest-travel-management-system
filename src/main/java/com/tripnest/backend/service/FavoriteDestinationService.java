package com.tripnest.backend.service;

import com.tripnest.backend.dto.response.DestinationResponse;
import com.tripnest.backend.dto.response.FavoriteDestinationResponse;

import java.util.List;

public interface FavoriteDestinationService {
    FavoriteDestinationResponse addFavorite(Long destinationId);
    void removeFavorite(Long destinationId);
    List<FavoriteDestinationResponse> getUserFavorites();
    boolean isFavorited(Long destinationId);
    List<DestinationResponse> getMostFavoritedDestinations();
}
