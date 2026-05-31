package com.socialnetwork.feed.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class PostDto {
    private int id;
    private String imageUrl;
    private String caption;
    private Date createdAt;
    private ProfileDto createdBy;
    private List<CommentDto> comments;
    private Set<ProfileDto> userLikes;
}
