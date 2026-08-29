package com.tripnest.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(name = "preferred_travel_type")
    private String preferredTravelType; // Solo, Family, Friends, Couple

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_destinations", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "destination_name")
    @Builder.Default
    private List<String> preferredDestinations = new ArrayList<>();

    @Column(name = "budget_range")
    private String budgetRange; // Budget, Moderate, Luxury, or custom $ range

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_activities", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "activity_name")
    @Builder.Default
    private List<String> preferredActivities = new ArrayList<>(); // Adventure, Beaches, Nature, Historical, etc.

    @Column(name = "preferred_transportation")
    private String preferredTransportation; // Flight, Train, Car, Bus, Cruise

    @Column(name = "preferred_accommodation_type")
    private String preferredAccommodationType; // Hotel, Hostel, Resort, Villa, Apartment

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
