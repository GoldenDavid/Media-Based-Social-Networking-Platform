package com.socialnetwork.service.feed;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.UserPrincipal;
import com.socialnetwork.dto.feed.GetFeedResponse;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;
import com.socialnetwork.model.UserFollowing;
import com.socialnetwork.repository.FollowerRepository;
import com.socialnetwork.repository.PostRepository;
import com.socialnetwork.service.profile.ProfileService;
import com.socialnetwork.util.MapperUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("dynamicFeedService")
@RequiredArgsConstructor
public class DynamicFeedServiceImpl implements FeedService {
  private final ProfileService profileService;
  private final PostRepository postRepository;
  private final FollowerRepository followerRepository;

  @Override
  public GetFeedResponse getFeed(UserPrincipal userPrincipal, int limit, int page) {
    Profile profile = profileService.getProfileEntity(userPrincipal);

    List<UserFollowing> followings = followerRepository.findByFollowerUserId(profile.getId());
    List<Integer> followingProfileIdList = followings.stream().map(following -> following.getFollowingUserId())
        .toList();
    log.info("followingProfileIdList={}", followingProfileIdList);
    int totalPost = postRepository.countByCreatedByIn(followingProfileIdList);
    log.info("totalPost={}", totalPost);
    int totalPage = (int) Math.ceil((double) totalPost / limit);
    int offset = (page - 1) * limit;

    List<PostDto> posts = postRepository
        .findByCreatedBy(followingProfileIdList, limit, offset)
        .stream().map(MapperUtils::toDto).toList();

    return GetFeedResponse.builder()
        .posts(posts).totalPage(totalPage).build();
  }

}
