package com.socialnetwork.profile.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated list of profiles that {@code profileId} follows, returned by
 * {@code GET /follow/user/followings/{id}} wrapped in {@code BaseResponse}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowingsResponse {
    private int totalPage;
    private int totalCount;
    private List<ProfileDto> followings;
}
