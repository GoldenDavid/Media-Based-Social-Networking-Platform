package com.socialnetwork.post.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialnetwork.post.model.Story;

@Repository
public interface StoryRepository extends JpaRepository<Story, Integer> {
    List<Story> findByCreatedByProfileIdInAndCreatedAtAfter(List<Integer> profileIds, Date createdAt);
}
