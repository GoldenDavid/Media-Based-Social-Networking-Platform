package com.socialnetwork.service.profile;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.profile.GetFollowerResponse;
import com.socialnetwork.dto.profile.GetFollowingResponse;

public interface FollowerService {
  void folowUser(UserPrincipal userPrincipal, int profileId);

  void unfolowUser(UserPrincipal userPrincipal, int profileId);

  GetFollowerResponse getFollowers(int profileId, int page, int limit);

  GetFollowingResponse getFollowings(int profileId, int page, int limit);

}
