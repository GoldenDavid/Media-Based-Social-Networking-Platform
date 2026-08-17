package com.socialnetwork.feed.grpc;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.socialnetwork.feed.dto.PostDto;
import com.socialnetwork.feed.dto.ProfileDto;
import com.socialnetwork.grpc.post.CountPostsByAuthorsRequest;
import com.socialnetwork.grpc.post.CountPostsResponse;
import com.socialnetwork.grpc.post.GetPostsByAuthorsRequest;
import com.socialnetwork.grpc.post.GetPostsByIdsRequest;
import com.socialnetwork.grpc.post.PostResponse;
import com.socialnetwork.grpc.post.PostServiceGrpc;
import com.socialnetwork.grpc.post.PostsResponse;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceGrpcClient {

    @GrpcClient("post-service")
    private PostServiceGrpc.PostServiceBlockingStub stub;

    private final ProfileServiceGrpcClient profileServiceGrpcClient;

    public List<PostDto> getPostsByIds(List<Integer> postIds) {
        if (postIds == null || postIds.isEmpty()) return List.of();

        try {
            PostsResponse response = stub.getPostsByIds(
                GetPostsByIdsRequest.newBuilder().addAllPostIds(postIds).build());
            return hydrateAuthors(response.getPostsList().stream().map(this::toDto).toList());
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetPostsByIds failed: {}", e.getStatus());
            return List.of();
        }
    }

    public List<PostDto> getPostsByAuthors(List<Integer> authorIds, int limit, int offset) {
        if (authorIds == null || authorIds.isEmpty()) return List.of();

        try {
            PostsResponse response = stub.getPostsByAuthors(
                GetPostsByAuthorsRequest.newBuilder()
                    .addAllAuthorProfileIds(authorIds)
                    .setLimit(limit)
                    .setOffset(offset)
                    .build());
            return hydrateAuthors(response.getPostsList().stream().map(this::toDto).toList());
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetPostsByAuthors failed: {}", e.getStatus());
            return List.of();
        }
    }

    public int countPostsByAuthors(List<Integer> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) return 0;
        try {
            CountPostsResponse response = stub.countPostsByAuthors(
                CountPostsByAuthorsRequest.newBuilder().addAllAuthorProfileIds(authorIds).build());
            return response.getCount();
        } catch (StatusRuntimeException e) {
            log.error("gRPC CountPostsByAuthors failed: {}", e.getStatus());
            return 0;
        }
    }

    private List<PostDto> hydrateAuthors(List<PostDto> posts) {
        java.util.Set<Integer> profileIds = new java.util.HashSet<>();
        
        posts.forEach(p -> {
            if (p.getCreatedBy() != null) {
                profileIds.add(p.getCreatedBy().getId());
            }
            if (p.getUserLikes() != null) {
                p.getUserLikes().forEach(liker -> profileIds.add(liker.getId()));
            }
        });

        if (profileIds.isEmpty()) {
            return posts;
        }

        List<Integer> idsList = new java.util.ArrayList<>(profileIds);

        // Single batch gRPC call to profile-service.
        List<ProfileDto> resolved = profileServiceGrpcClient.getProfilesByIds(idsList);

        // Build id -> ProfileDto map; nulls (missing profiles) are dropped.
        Map<Integer, ProfileDto> profilesById = new java.util.HashMap<>();
        for (ProfileDto p : resolved) {
            if (p != null) {
                profilesById.put(p.getId(), p);
            }
        }

        posts.forEach(p -> {
            if (p.getCreatedBy() != null) {
                ProfileDto hydrated = profilesById.get(p.getCreatedBy().getId());
                if (hydrated != null) {
                    p.setCreatedBy(hydrated);
                }
            }
            if (p.getUserLikes() != null) {
                java.util.Set<ProfileDto> hydratedLikes = new java.util.HashSet<>();
                p.getUserLikes().forEach(liker -> {
                    ProfileDto hydrated = profilesById.get(liker.getId());
                    if (hydrated != null) {
                        hydratedLikes.add(hydrated);
                    }
                });
                p.setUserLikes(hydratedLikes);
            }
        });
        return posts;
    }

    private PostDto toDto(PostResponse r) {
        java.util.Set<ProfileDto> unhydratedLikes = r.getUserLikesProfileIdsList().stream()
            .map(id -> ProfileDto.builder().id(id).build())
            .collect(java.util.stream.Collectors.toSet());
            
        return PostDto.builder()
            .id(r.getId())
            .imageUrl(r.getImageUrl())
            .caption(r.getCaption())
            .createdAt(new java.util.Date(r.getCreatedAt()))
            .createdBy(ProfileDto.builder().id(r.getCreatedByProfileId()).build())
            .userLikes(unhydratedLikes)
            .build();
    }
}
