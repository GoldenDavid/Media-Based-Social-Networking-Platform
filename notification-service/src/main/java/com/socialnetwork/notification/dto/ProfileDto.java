package com.socialnetwork.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileDto {
    private int id;
    private String displayName;
    private String username;
    private String profileImageUrl;
}
