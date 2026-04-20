package com.socialnetwork.service.feed;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.GetFeedResponse;

public interface FeedService {
  GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page);
}
