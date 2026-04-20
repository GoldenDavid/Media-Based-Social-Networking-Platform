package com.socialnetwork.service.profile;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.profile.UpdateProfileImageRequest;
import com.socialnetwork.dto.profile.UpdateProfileRequest;
import com.socialnetwork.exception.UserNotFoundException;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.ProfileRepository;
import com.socialnetwork.service.UploadService;

@Service
public class ProfileServiceImpl implements ProfileService {
  @Autowired
  private UploadService uploadService;
  @Autowired
  private ProfileRepository profileRepository;

  @Override
  public Profile getUserProfile(UserPrincipal userPrincipal) {
    Profile profile = profileRepository.findOneByUserId(userPrincipal.getId().toString());
    if (Objects.isNull(profile)) {
      profile = new Profile();
      profile.setUserId(userPrincipal.getId().toString());
      profile.setDisplayName(userPrincipal.getName());
      profileRepository.save(profile);
    }
    return profile;
  }

  @Override
  public Profile getUserProfile(int id) {
    return profileRepository.findById(id).orElseThrow(UserNotFoundException::new);
  }

  @Override
  public Profile updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
    Profile profile = this.getUserProfile(userPrincipal);
    profile.setBio(request.getBio());
    profile.setDisplayName(request.getDisplayName());
    profile.setUsername(request.getUsername());
    profileRepository.save(profile);
    return profile;
  }

  @Override
  public Profile updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request) {
    String url = uploadService.uploadImage(request.getBase64ImageString());
    Profile profile = this.getUserProfile(userPrincipal);
    profile.setProfileImageUrl(url);
    profileRepository.save(profile);
    return profile;
  }
}
