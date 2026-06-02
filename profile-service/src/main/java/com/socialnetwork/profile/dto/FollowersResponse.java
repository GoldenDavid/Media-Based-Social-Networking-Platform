package com.socialnetwork.profile.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated list of profiles that follow {@code profileId}, returned by
 * {@code GET /follow/user/followers/{id}} wrapped in {@code BaseResponse}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowersResponse {
    private int totalPage;
    private int totalCount;
    private List<ProfileDto> followers;
}
