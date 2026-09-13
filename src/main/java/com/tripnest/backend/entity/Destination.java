package com.tripnest.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @Column(name = "city")
    private String city;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "best_time_to_visit")
    private String bestTimeToVisit;

    @Column(name = "category")
    private String category; // Beaches, Mountains, Historical, Nature, Cultural, Urban

    @Column(name = "is_popular")
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "favorite_count")
    @Builder.Default
    private Long favoriteCount = 0L;

    @OneToMany(
        mappedBy = "destination",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Builder.Default
    private java.util.List<Attraction> attractions = new java.util.ArrayList<>();

    @Column(name = "currency")
    private String currency;

    @Column(name = "language")
    private String language;

    @Column(name = "climate")
    private String climate;

    @Column(name = "transportation")
    private String transportation;

    @Column(name = "visa_requirements")
    private String visaRequirements;

    @Column(name = "time_zone")
    private String timeZone;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "destination_travel_tips", joinColumns = @JoinColumn(name = "destination_id"))
    @Column(name = "tip", length = 1000)
    @Builder.Default
    private java.util.List<String> travelTips = new java.util.ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addAttraction(Attraction attraction) {
        attractions.add(attraction);
        attraction.setDestination(this);
    }

    public void removeAttraction(Attraction attraction) {
        attractions.remove(attraction);
        attraction.setDestination(null);
    }
}