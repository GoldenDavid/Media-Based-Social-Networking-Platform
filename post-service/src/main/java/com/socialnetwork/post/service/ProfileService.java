package com.socialnetwork.post.service;

import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.dto.UserPrincipal;
import com.socialnetwork.post.model.Profile;

public interface ProfileService {
    Profile getProfileEntity(UserPrincipal userPrincipal);
    Profile getProfileEntity(int id);
    ProfileDto getUserProfile(int id);
}
