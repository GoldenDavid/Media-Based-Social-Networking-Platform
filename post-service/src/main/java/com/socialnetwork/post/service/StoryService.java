package com.socialnetwork.post.service;

import com.socialnetwork.post.dto.StoryDto;
import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.common.security.UserPrincipal;

public interface StoryService {
    StoryDto createStory(UserPrincipal userPrincipal, CreatePostRequest request);
}
