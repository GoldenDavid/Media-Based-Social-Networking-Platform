package com.socialnetwork.profile.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.socialnetwork.profile.model.ProfileDocument;

import java.util.List;

@Repository
public interface ProfileSearchRepository extends ElasticsearchRepository<ProfileDocument, String> {

    List<ProfileDocument> findByUsernameContainingOrDisplayNameContaining(String username, String displayName);
}
