package com.socialnetwork.feed.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoryFeedDto {
    private ProfileDto author;
    private List<StoryDto> stories;
}
