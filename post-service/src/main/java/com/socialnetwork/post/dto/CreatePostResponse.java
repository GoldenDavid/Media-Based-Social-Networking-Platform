package com.socialnetwork.post.dto;

import com.socialnetwork.post.dto.PostDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreatePostResponse {
  private PostDto post;
}
