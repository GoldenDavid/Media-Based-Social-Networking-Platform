package com.socialnetwork.profile.service;

import com.socialnetwork.profile.dto.ProfileDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.profile.model.Profile;
import com.socialnetwork.profile.model.UserFollowing;
import com.socialnetwork.profile.repository.FollowerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowerService {

    private final ProfileService profileService;
    private final FollowerRepository followerRepository;

    @Transactional
    public void followUser(UserPrincipal userPrincipal, int targetProfileId) {
        Profile profile = profileService.getOrCreateProfile(userPrincipal);
        if (profile.getId() == targetProfileId) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        UserFollowing existing = followerRepository
                .findByFollowerUserIdAndFollowingUserId(profile.getId(), targetProfileId);
        if (Objects.nonNull(existing)) return; // idempotent

        UserFollowing following = UserFollowing.builder()
                .followerUserId(profile.getId())
                .followingUserId(targetProfileId)
                .createdAt(new Date())
                .build();
        followerRepository.save(following);
        log.info("User {} now follows profile {}", profile.getId(), targetProfileId);
    }

    @Transactional
    public void unfollowUser(UserPrincipal userPrincipal, int targetProfileId) {
        Profile profile = profileService.getOrCreateProfile(userPrincipal);
        UserFollowing existing = followerRepository
                .findByFollowerUserIdAndFollowingUserId(profile.getId(), targetProfileId);
        if (Objects.isNull(existing)) return; // idempotent
        followerRepository.delete(existing);
        log.info("User {} unfollowed profile {}", profile.getId(), targetProfileId);
    }

    public List<ProfileDto> getFollowers(int profileId, int page, int limit) {
        int offset = (page - 1) * limit;
        return followerRepository.findByFollowingUserId(profileId, limit, offset)
                .stream()
                .map(uf -> profileService.getUserProfile(uf.getFollowerUserId()))
                .toList();
    }

    public int countFollowers(int profileId) {
        return followerRepository.countByFollowingUserId(profileId);
    }

    public List<ProfileDto> getFollowings(int profileId, int page, int limit) {
        int offset = (page - 1) * limit;
        return followerRepository.findByFollowerUserId(profileId, limit, offset)
                .stream()
                .map(uf -> profileService.getUserProfile(uf.getFollowingUserId()))
                .toList();
    }

    public int countFollowings(int profileId) {
        return followerRepository.countByFollowerUserId(profileId);
    }
}
