package com.socialnetwork.dto.profile;

import java.util.List;

import com.socialnetwork.dto.ProfileDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GetFollowerResponse {
  private List<ProfileDto> followers;
  private int totalPage;
}
