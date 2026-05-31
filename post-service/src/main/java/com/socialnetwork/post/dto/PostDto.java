package com.socialnetwork.post.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDto {
    private int id;
    private ProfileDto createdBy;
    private String imageUrl;
    private String caption;
    private Date createdAt;
    private List<CommentDto> comments;
    private Set<ProfileDto> userLikes;
}
