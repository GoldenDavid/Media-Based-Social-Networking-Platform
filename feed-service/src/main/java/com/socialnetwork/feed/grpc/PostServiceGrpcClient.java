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
        // Collect distinct author profile IDs (ADR-010: batch gRPC, no N+1).
        List<Integer> authorIds = posts.stream()
            .map(PostDto::getCreatedBy)
            .filter(Objects::nonNull)
            .map(ProfileDto::getId)
            .distinct()
            .toList();

        if (authorIds.isEmpty()) {
            return posts;
        }

        // Single batch gRPC call to profile-service.
        List<ProfileDto> resolved = profileServiceGrpcClient.getProfilesByIds(authorIds);

        // Build id -> ProfileDto map; nulls (missing profiles) are dropped.
        Map<Integer, ProfileDto> profilesById = new java.util.HashMap<>();
        for (int i = 0; i < authorIds.size() && i < resolved.size(); i++) {
            ProfileDto p = resolved.get(i);
            if (p != null) {
                profilesById.put(authorIds.get(i), p);
            }
        }

        posts.forEach(p -> {
            if (p.getCreatedBy() != null) {
                ProfileDto hydrated = profilesById.get(p.getCreatedBy().getId());
                if (hydrated != null) {
                    p.setCreatedBy(hydrated);
                }
            }
        });
        return posts;
    }

    private PostDto toDto(PostResponse r) {
        return PostDto.builder()
            .id(r.getId())
            .imageUrl(r.getImageUrl())
            .caption(r.getCaption())
            .createdAt(new java.util.Date(r.getCreatedAt()))
            .createdBy(ProfileDto.builder().id(r.getCreatedByProfileId()).build())
            .build();
    }
}
