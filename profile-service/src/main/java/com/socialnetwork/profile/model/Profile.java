package com.socialnetwork.profile.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Owns the profile row in the {@code profile} table.
 * This entity is the source of truth for profile data — other services
 * resolve profile info via the ProfileService gRPC API, not via direct DB access.
 */
@Entity
@Table(name = "profile")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(unique = true, name = "userId", nullable = false)
    String userId;

    String profileImageUrl;
    String displayName;
    String username;
    String bio;
}
