package com.socialnetwork.feed.grpc;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.socialnetwork.feed.dto.PostDto;
import com.socialnetwork.feed.dto.ProfileDto;
import com.socialnetwork.feed.dto.CommentDto;
import com.socialnetwork.grpc.post.GetPostsByAuthorsRequest;
import com.socialnetwork.grpc.post.GetPostsByIdsRequest;
import com.socialnetwork.grpc.post.PostResponse;
import com.socialnetwork.grpc.post.PostServiceGrpc;
import com.socialnetwork.grpc.post.PostsResponse;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;

/**
 * gRPC client for the post-service.
 * Used by PrecomputedFeedServiceImpl (getPostsByIds) and DynamicFeedServiceImpl (getPostsByAuthors).
 */
@Slf4j
@Service
public class PostServiceGrpcClient {

    @GrpcClient("post-service")
    private PostServiceGrpc.PostServiceBlockingStub stub;

    /**
     * Used by PrecomputedFeedServiceImpl to hydrate post IDs from Redis into full post DTOs.
     */
    public List<PostDto> getPostsByIds(List<Integer> postIds) {
        if (postIds == null || postIds.isEmpty()) return List.of();
        
        try {
            PostsResponse response = stub.getPostsByIds(
                GetPostsByIdsRequest.newBuilder().addAllPostIds(postIds).build());
            return response.getPostsList().stream().map(this::toDto).collect(Collectors.toList());
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetPostsByIds failed: {}", e.getStatus());
            return List.of();
        }
    }

    /**
     * Used by DynamicFeedServiceImpl to get paginated posts from followed authors.
     */
    public List<PostDto> getPostsByAuthors(List<Integer> authorIds, int limit, int offset) {
        if (authorIds == null || authorIds.isEmpty()) return List.of();
        
        try {
            PostsResponse response = stub.getPostsByAuthors(
                GetPostsByAuthorsRequest.newBuilder()
                    .addAllAuthorProfileIds(authorIds)
                    .setLimit(limit)
                    .setOffset(offset)
                    .build());
            return response.getPostsList().stream().map(this::toDto).collect(Collectors.toList());
        } catch (StatusRuntimeException e) {
            log.error("gRPC GetPostsByAuthors failed: {}", e.getStatus());
            return List.of();
        }
    }

    private PostDto toDto(PostResponse r) {
        // Hydrate only what is returned by the gRPC stub.
        // For a real feed, we might need to fetch full ProfileDto for the author here,
        // or we just rely on the client to fetch profile details if needed, 
        // but let's map the ID for now.
        return PostDto.builder()
            .id(r.getId())
            .imageUrl(r.getImageUrl())
            .caption(r.getCaption())
            .createdAt(new java.util.Date(r.getCreatedAt()))
            .createdBy(ProfileDto.builder().id(r.getCreatedByProfileId()).build())
            .build();
    }
}
