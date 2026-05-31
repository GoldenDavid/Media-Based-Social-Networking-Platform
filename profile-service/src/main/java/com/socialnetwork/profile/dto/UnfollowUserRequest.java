package com.socialnetwork.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UnfollowUserRequest {
    @NotNull
    private Integer profileId;
}
