package com.socialnetwork.post.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.socialnetwork.post.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
  List<Post> findByIdIn(List<Integer> ids);
  
  List<Post> findByCreatedByProfileId(int profileId);

  @Query("SELECT COUNT(p) FROM Post p WHERE p.createdByProfileId IN :profileIds")
  int countByCreatedByIn(@Param("profileIds") List<Integer> profileIds);

  @Query(value = "SELECT * FROM post WHERE created_by_id IN :profileIds ORDER BY created_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
  List<Post> findByCreatedBy(@Param("profileIds") List<Integer> profileIds, @Param("limit") int limit, @Param("offset") int offset);

  @Query("SELECT p FROM Post p JOIN p.userSavedProfileIds s WHERE s = :profileId ORDER BY p.createdAt DESC")
  List<Post> findPostsSavedByUser(@Param("profileId") int profileId);
}
