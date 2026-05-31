package com.socialnetwork.feed.service;

import com.socialnetwork.feed.dto.GetFeedResponse;
import com.socialnetwork.feed.dto.UserPrincipal;

public interface FeedService {
    GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page);
}
