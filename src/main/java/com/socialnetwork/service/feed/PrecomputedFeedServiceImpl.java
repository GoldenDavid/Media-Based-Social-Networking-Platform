package com.socialnetwork.service.feed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.GetFeedResponse;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import com.socialnetwork.repository.FeedRepository;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.profile.ProfileService;
import com.socialnetwork.util.MapperUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("precomputedFeedService")
@RequiredArgsConstructor
public class PrecomputedFeedServiceImpl implements FeedService {
  private final ProfileService profileService;
  private final PostRepository postRepository;
  private final FeedRepository feedRepository;

  @Override
  public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
    Profile profile = profileService.getProfileEntity(userPrincipal);

    List<Long> postIds = feedRepository.getFeed(profile.getId(), limit, page);
    log.info("postIds={}", postIds);

    List<PostDto> posts = postRepository.findAllById(postIds.stream().map(Long::intValue).toList())
        .stream().map(MapperUtils::toDto).toList();

    Long totalPost = feedRepository.getFeedSize(profile.getId());
    log.info("totalPost={}", totalPost);
    int totalPage = (int) Math.ceil((double) totalPost / limit);

    return GetFeedResponse.builder()
        .posts(posts).totalPage(totalPage).build();
  }

}
