package com.socialnetwork.profile.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequest {
    @NotBlank
    private String displayName;
    private String username;
    private String bio;
}
