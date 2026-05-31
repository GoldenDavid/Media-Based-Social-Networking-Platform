package com.socialnetwork.post.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private int id;
    private String comment;
    private ProfileDto createdBy;
    private Date createdAt;
}
