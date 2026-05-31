package com.socialnetwork.profile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowUserRequest {
    @NotNull
    private Integer profileId;
}
