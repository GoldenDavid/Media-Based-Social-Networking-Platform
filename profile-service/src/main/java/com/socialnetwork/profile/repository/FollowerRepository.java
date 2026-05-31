package com.socialnetwork.profile.repository;

import com.socialnetwork.profile.model.UserFollowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowerRepository extends JpaRepository<UserFollowing, Integer> {

    @Query(value = "SELECT * FROM user_following WHERE follower_user_id = :followerUserId LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<UserFollowing> findByFollowerUserId(@Param("followerUserId") int followerUserId,
                                             @Param("limit") int limit,
                                             @Param("offset") int offset);

    int countByFollowerUserId(int followerUserId);

    /** Full list — used by feed fan-out (no pagination needed). */
    List<UserFollowing> findByFollowerUserId(int followerUserId);

    /** Full list — used by PushFeedConsumer fan-out. */
    List<UserFollowing> findByFollowingUserId(int followingUserId);

    @Query(value = "SELECT * FROM user_following WHERE following_user_id = :followingUserId LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<UserFollowing> findByFollowingUserId(@Param("followingUserId") int followingUserId,
                                              @Param("limit") int limit,
                                              @Param("offset") int offset);

    int countByFollowingUserId(int followingUserId);

    UserFollowing findByFollowerUserIdAndFollowingUserId(int followerUserId, int followingUserId);
}
