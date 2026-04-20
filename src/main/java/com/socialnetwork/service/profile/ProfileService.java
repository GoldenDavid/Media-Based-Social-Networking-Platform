package com.socialnetwork.service.profile;

import com.socialnetwork.dto.ProfileDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.profile.UpdateProfileImageRequest;
import com.socialnetwork.dto.profile.UpdateProfileRequest;

import com.socialnetwork.model.Profile;

public interface ProfileService {
  ProfileDto getUserProfile(UserPrincipal userPrincipal);

  ProfileDto getUserProfile(int id);

  Profile getProfileEntity(int id);

  Profile getProfileEntity(UserPrincipal userPrincipal);

  ProfileDto updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request);

  ProfileDto updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request);
}
