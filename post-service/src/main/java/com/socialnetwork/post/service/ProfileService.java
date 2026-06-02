package com.socialnetwork.post.service;

import java.util.List;

import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.common.security.UserPrincipal;

/**
 * Post-service's view of the Profile Service.
 * All methods return ProfileDto — no JPA Profile entity is used in this service.
 */
public interface ProfileService {
    ProfileDto getProfile(UserPrincipal userPrincipal);
    ProfileDto getProfile(int profileId);
    ProfileDto getUserProfile(int profileId);
    List<Integer> getFollowerIds(int profileId);
}
