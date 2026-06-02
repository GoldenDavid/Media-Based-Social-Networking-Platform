package com.socialnetwork.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for {@code POST /follow} and {@code DELETE /follow}.
 * Wrapped in {@code BaseResponse}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {
    private boolean followed;
    private int profileId;
}
