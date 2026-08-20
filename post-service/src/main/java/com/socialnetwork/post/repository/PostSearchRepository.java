package com.socialnetwork.post.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.socialnetwork.post.model.PostDocument;

import java.util.List;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {

    List<PostDocument> findByContentContainingIgnoreCase(String content);
}
