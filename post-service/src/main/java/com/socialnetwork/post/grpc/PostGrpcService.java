package com.socialnetwork.post.grpc;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.socialnetwork.grpc.post.CountPostsByAuthorsRequest;
import com.socialnetwork.grpc.post.CountPostsResponse;
import com.socialnetwork.grpc.post.GetPostRequest;
import com.socialnetwork.grpc.post.GetPostsByAuthorsRequest;
import com.socialnetwork.grpc.post.GetPostsByIdsRequest;
import com.socialnetwork.grpc.post.PostResponse;
import com.socialnetwork.grpc.post.PostServiceGrpc;
import com.socialnetwork.grpc.post.PostsResponse;
import com.socialnetwork.post.model.Post;
import com.socialnetwork.post.repository.PostRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostGrpcService extends PostServiceGrpc.PostServiceImplBase {

    private final PostRepository postRepository;

    @Override
    public void getPost(GetPostRequest request, StreamObserver<PostResponse> responseObserver) {
        log.info("gRPC GetPost: {}", request.getPostId());
        try {
            Post post = postRepository.findById(request.getPostId()).orElse(null);
            if (post == null) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Post not found: " + request.getPostId())
                        .asRuntimeException());
                return;
            }
            responseObserver.onNext(toPostResponse(post));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC GetPost error", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getPostsByIds(GetPostsByIdsRequest request, StreamObserver<PostsResponse> responseObserver) {
        log.info("gRPC GetPostsByIds: size={}", request.getPostIdsCount());
        try {
            List<Post> posts = postRepository.findAllById(request.getPostIdsList());
            
            // Map and preserve order if possible, though PostRepository might not guarantee order.
            List<PostResponse> postResponses = posts.stream()
                    .map(this::toPostResponse)
                    .collect(Collectors.toList());

            responseObserver.onNext(PostsResponse.newBuilder()
                    .addAllPosts(postResponses)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC GetPostsByIds error", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
        }
    }

    @Override
    public void countPostsByAuthors(CountPostsByAuthorsRequest request, StreamObserver<CountPostsResponse> responseObserver) {
        log.info("gRPC CountPostsByAuthors: size={}", request.getAuthorProfileIdsCount());
        try {
            int count = postRepository.countByCreatedByIn(request.getAuthorProfileIdsList());
            responseObserver.onNext(CountPostsResponse.newBuilder().setCount(count).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC CountPostsByAuthors error", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getPostsByAuthors(GetPostsByAuthorsRequest request, StreamObserver<PostsResponse> responseObserver) {
        log.info("gRPC GetPostsByAuthors: size={}, limit={}, offset={}", 
                request.getAuthorProfileIdsCount(), request.getLimit(), request.getOffset());
        try {
            List<Post> posts = postRepository.findByCreatedBy(
                    request.getAuthorProfileIdsList(), 
                    request.getLimit(), 
                    request.getOffset());

            List<PostResponse> postResponses = posts.stream()
                    .map(this::toPostResponse)
                    .collect(Collectors.toList());

            responseObserver.onNext(PostsResponse.newBuilder()
                    .addAllPosts(postResponses)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC GetPostsByAuthors error", e);
            responseObserver.onError(Status.INTERNAL.withCause(e).asRuntimeException());
        }
    }

    private PostResponse toPostResponse(Post post) {
        return PostResponse.newBuilder()
                .setId(post.getId())
                .setImageUrl(post.getImageUrl() != null ? post.getImageUrl() : "")
                .setCaption(post.getCaption() != null ? post.getCaption() : "")
                .setCreatedByProfileId(post.getCreatedBy() != null ? post.getCreatedBy().getId() : 0)
                .setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().getTime() : 0)
                .setLikeCount(post.getUserLikes() != null ? post.getUserLikes().size() : 0)
                .setCommentCount(post.getComments() != null ? post.getComments().size() : 0)
                .build();
    }
}
