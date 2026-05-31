package com.socialnetwork.profile.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateProfileImageRequest {
    @NotBlank
    private String base64ImageString;
}
