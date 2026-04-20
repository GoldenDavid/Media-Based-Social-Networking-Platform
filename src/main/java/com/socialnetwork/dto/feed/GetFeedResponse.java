package com.socialnetwork.dto.feed;

import java.util.List;

import com.socialnetwork.dto.PostDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GetFeedResponse {
  private List<PostDto> posts;
  private int totalPage;
}
