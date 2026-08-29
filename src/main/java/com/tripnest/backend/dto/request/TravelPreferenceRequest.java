package com.tripnest.backend.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPreferenceRequest {
    private String preferredTravelType; // Solo, Family, Friends, Couple
    private List<String> preferredDestinations;
    private String budgetRange; // Budget, Moderate, Luxury, or custom
    private List<String> preferredActivities; // Adventure, Beaches, Nature, Historical, etc.
    private String preferredTransportation; // Flight, Train, Car, Bus, Cruise
    private String preferredAccommodationType; // Hotel, Hostel, Resort, Villa, Apartment
}
