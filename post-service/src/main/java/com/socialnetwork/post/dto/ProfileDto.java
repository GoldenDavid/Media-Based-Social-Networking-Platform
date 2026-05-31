package com.socialnetwork.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    private int id;
    private String displayName;
    private String username;
    private String bio;
    private String profileImageUrl;
    private String userId;
}
