package com.socialnetwork.dto.profile;

import com.socialnetwork.dto.ProfileDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GetProfileResponse {
  ProfileDto profile;
  int numberOfPost;
  int numberOfFollower;
  int numberOfFollowing;
}
