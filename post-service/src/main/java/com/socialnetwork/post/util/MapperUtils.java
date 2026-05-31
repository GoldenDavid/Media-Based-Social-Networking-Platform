package com.socialnetwork.post.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.socialnetwork.post.dto.CommentDto;
import com.socialnetwork.post.dto.PostDto;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.model.Comment;
import com.socialnetwork.post.model.Post;

public class MapperUtils {

    public static CommentDto toDto(Comment comment, ProfileDto createdByProfile) {
        if (Objects.isNull(comment)) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .createdBy(createdByProfile)
                .build();
    }

    public static PostDto toDto(Post post, ProfileDto createdByProfile, List<CommentDto> comments, Set<ProfileDto> likes) {
        if (Objects.isNull(post)) {
            return null;
        }

        return PostDto.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .createdBy(createdByProfile)
                .comments(comments)
                .userLikes(likes)
                .build();
    }
}
