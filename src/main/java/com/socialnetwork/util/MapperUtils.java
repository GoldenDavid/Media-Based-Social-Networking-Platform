package com.socialnetwork.util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.socialnetwork.dto.CommentDto;
import com.socialnetwork.dto.PostDto;
import com.socialnetwork.dto.ProfileDto;
import com.socialnetwork.model.Comment;
import com.socialnetwork.model.Post;
import com.socialnetwork.model.Profile;

public class MapperUtils {

    public static ProfileDto toDto(Profile profile) {
        if (Objects.isNull(profile)) {
            return null;
        }
        return ProfileDto.builder()
                .id(profile.getId())
                .profileImageUrl(profile.getProfileImageUrl())
                .displayName(profile.getDisplayName())
                .username(profile.getUsername())
                .bio(profile.getBio())
                .build();
    }

    public static CommentDto toDto(Comment comment) {
        if (Objects.isNull(comment)) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .comment(comment.getComment())
                .createdAt(comment.getCreatedAt())
                .createdBy(toDto(comment.getCreatedBy()))
                .build();
    }

    public static PostDto toDto(Post post) {
        if (Objects.isNull(post)) {
            return null;
        }
        List<CommentDto> comments = Objects.isNull(post.getComments()) ? Collections.emptyList()
                : post.getComments().stream().map(MapperUtils::toDto).collect(Collectors.toList());
        
        Set<ProfileDto> likes = Objects.isNull(post.getUserLikes()) ? Collections.emptySet()
                : post.getUserLikes().stream().map(MapperUtils::toDto).collect(Collectors.toSet());

        return PostDto.builder()
                .id(post.getId())
                .caption(post.getCaption())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .createdBy(toDto(post.getCreatedBy()))
                .comments(comments)
                .userLikes(likes)
                .build();
    }
}
