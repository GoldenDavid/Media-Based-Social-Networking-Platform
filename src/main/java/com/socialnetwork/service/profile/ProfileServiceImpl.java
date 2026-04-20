package com.socialnetwork.service.profile;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.dto.ProfileDto;
import com.socialnetwork.exception.UserNotFoundException;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.ProfileRepository;
import com.socialnetwork.service.UploadService;
import com.socialnetwork.util.MapperUtils;

@Service
public class ProfileServiceImpl implements ProfileService {
  private final UploadService uploadService;
  private final ProfileRepository profileRepository;

  public ProfileServiceImpl(UploadService uploadService, ProfileRepository profileRepository) {
    this.uploadService = uploadService;
    this.profileRepository = profileRepository;
  }

  @Override
  public ProfileDto getUserProfile(UserPrincipal userPrincipal) {
    return MapperUtils.toDto(getProfileEntity(userPrincipal));
  }

  @Transactional
  public Profile getProfileEntity(UserPrincipal userPrincipal) {
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
  public ProfileDto getUserProfile(int id) {
    return MapperUtils.toDto(profileRepository.findById(id).orElseThrow(UserNotFoundException::new));
  }

  @Override
  @Transactional
  public ProfileDto updateProfile(UserPrincipal userPrincipal, UpdateProfileRequest request) {
    Profile profile = this.getProfileEntity(userPrincipal);
    profile.setBio(request.getBio());
    profile.setDisplayName(request.getDisplayName());
    profile.setUsername(request.getUsername());
    profileRepository.save(profile);
    return MapperUtils.toDto(profile);
  }

  @Override
  @Transactional
  public ProfileDto updateProfileImage(UserPrincipal userPrincipal, UpdateProfileImageRequest request) {
    String url = uploadService.uploadImage(request.getBase64ImageString());
    Profile profile = this.getProfileEntity(userPrincipal);
    profile.setProfileImageUrl(url);
    profileRepository.save(profile);
    return MapperUtils.toDto(profile);
  }
}
