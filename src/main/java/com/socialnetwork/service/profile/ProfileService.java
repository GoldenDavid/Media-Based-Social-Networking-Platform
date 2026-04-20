package com.socialnetwork.service.profile;

import com.socialnetwork.dto.ProfileDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.profile.UpdateProfileImageRequest;
import com.socialnetwork.dto.profile.UpdateProfileRequest;

public interface ProfileService {
  ProfileDto getUserProfile(UserPrincipal userPrincipal);

  ProfileDto getUserProfile(int id);

  ProfileDto updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request);

  ProfileDto updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request);
}
