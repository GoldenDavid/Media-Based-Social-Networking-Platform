package com.socialnetwork.dto.feed;

import com.socialnetwork.dto.PostDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class GetPostResponse {
  private PostDto post;
}
