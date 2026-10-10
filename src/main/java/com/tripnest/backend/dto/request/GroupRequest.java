package com.tripnest.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequest {
    @NotBlank(message = "Group name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}
