package com.socialnetwork.post.job;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.socialnetwork.post.model.Post;
import com.socialnetwork.post.model.PostDocument;
import com.socialnetwork.post.repository.PostRepository;
import com.socialnetwork.post.repository.PostSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchSyncJob {

    private final PostRepository postRepository;
    private final PostSearchRepository postSearchRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        log.info("Starting Elasticsearch initial sync for Post service...");
        syncData();
        log.info("Completed Elasticsearch initial sync for Post service.");
    }

    @Scheduled(cron = "${elasticsearch.sync.cron:0 0 2 * * ?}")
    public void syncOnSchedule() {
        log.info("Starting scheduled Elasticsearch sync for Post service...");
        syncData();
        log.info("Completed scheduled Elasticsearch sync for Post service.");
    }

    private void syncData() {
        int page = 0;
        int size = 500;
        Page<Post> postPage;

        do {
            postPage = postRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
            List<PostDocument> documents = postPage.getContent().stream()
                    .map(post -> PostDocument.builder()
                            .id(String.valueOf(post.getId()))
                            .createdByProfileId(String.valueOf(post.getCreatedByProfileId()))
                            .content(post.getCaption())
                            .imageUrl(post.getImageUrl())
                            .build())
                    .collect(Collectors.toList());

            if (!documents.isEmpty()) {
                postSearchRepository.saveAll(documents);
                log.info("Synced batch {} containing {} posts to Elasticsearch", page, documents.size());
            }
            page++;
        } while (postPage.hasNext());
    }
}
