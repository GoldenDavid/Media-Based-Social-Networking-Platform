package com.socialnetwork.feed.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class CommentDto {
    private int id;
    private String comment;
    private Date createdAt;
    private ProfileDto createdBy;
}
