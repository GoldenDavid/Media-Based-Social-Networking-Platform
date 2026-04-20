package com.socialnetwork.service.profile;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.profile.UpdateProfileImageRequest;
import com.socialnetwork.dto.profile.UpdateProfileRequest;
import com.socialnetwork.model.Profile;

public interface ProfileService {
  Profile getUserProfile(UserPrincipal userPrincipal);

  Profile getUserProfile(int id);

  Profile updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request);

  Profile updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request);
}
