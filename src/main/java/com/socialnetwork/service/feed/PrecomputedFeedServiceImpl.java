package com.socialnetwork.service.feed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.GetFeedResponse;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.FeedRepository;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.profile.ProfileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("precomputedFeedService")
public class PrecomputedFeedServiceImpl implements FeedService {
  @Autowired
  private ProfileService profileService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private FeedRepository feedRepository;

  @Override
  public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
    Profile profile = profileService.getUserProfile(userPrincipal);

    List<Long> postIds = feedRepository.getFeed(profile.getId(), limit, page);
    log.info("postIds={}", postIds);

    List<Post> posts = postRepository.findAllById(postIds.stream().map(Long::intValue).toList());

    Long totalPost = feedRepository.getFeedSize(profile.getId());
    log.info("totalPost={}", totalPost);
    int totalPage = (int) Math.ceil((double) totalPost / limit);

    return GetFeedResponse.builder()
        .posts(posts).totalPage(totalPage).build();
  }

}
