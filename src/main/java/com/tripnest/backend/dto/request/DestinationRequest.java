package com.tripnest.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationRequest {

    @NotBlank(message = "Destination name is required")
    private String name;

    @NotBlank(message = "Country is required")
    private String country;

    private String city;
    private String description;
    private String imageUrl;
    private String bestTimeToVisit;
    private String category;
    private Boolean isPopular;

    private String currency;
    private String language;
    private String climate;
    private String transportation;
    private String visaRequirements;
    private String timeZone;
    private java.util.List<String> travelTips;
}
