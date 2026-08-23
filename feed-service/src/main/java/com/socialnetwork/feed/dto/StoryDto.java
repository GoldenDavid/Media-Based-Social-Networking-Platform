package com.socialnetwork.feed.dto;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryDto {
    private int id;
    private ProfileDto createdBy;
    private String imageUrl;
    private Date createdAt;
}
