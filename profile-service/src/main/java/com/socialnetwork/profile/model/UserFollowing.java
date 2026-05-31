package com.socialnetwork.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Represents a follow relationship: {@code followerUserId} follows {@code followingUserId}.
 * The unique constraint prevents duplicate follow entries.
 */
@Entity
@Table(name = "user_following", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"followerUserId", "followingUserId"})
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFollowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    private int followerUserId;
    private int followingUserId;
    private Date createdAt;
}
