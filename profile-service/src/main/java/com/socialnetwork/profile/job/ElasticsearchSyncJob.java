package com.socialnetwork.profile.job;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.socialnetwork.profile.model.Profile;
import com.socialnetwork.profile.model.ProfileDocument;
import com.socialnetwork.profile.repository.ProfileRepository;
import com.socialnetwork.profile.repository.ProfileSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchSyncJob {

    private final ProfileRepository profileRepository;
    private final ProfileSearchRepository profileSearchRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartup() {
        log.info("Starting Elasticsearch initial sync for Profile service...");
        syncData();
        log.info("Completed Elasticsearch initial sync for Profile service.");
    }

    @Scheduled(cron = "${elasticsearch.sync.cron:0 0 2 * * ?}")
    public void syncOnSchedule() {
        log.info("Starting scheduled Elasticsearch sync for Profile service...");
        syncData();
        log.info("Completed scheduled Elasticsearch sync for Profile service.");
    }

    private void syncData() {
        int page = 0;
        int size = 500;
        Page<Profile> profilePage;

        do {
            profilePage = profileRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
            List<ProfileDocument> documents = profilePage.getContent().stream()
                    .map(p -> ProfileDocument.builder()
                            .id(String.valueOf(p.getId()))
                            .userId(p.getUserId())
                            .displayName(p.getDisplayName())
                            .username(p.getUsername())
                            .bio(p.getBio())
                            .profileImageUrl(p.getProfileImageUrl())
                            .build())
                    .collect(Collectors.toList());

            if (!documents.isEmpty()) {
                profileSearchRepository.saveAll(documents);
                log.info("Synced batch {} containing {} profiles to Elasticsearch", page, documents.size());
            }
            page++;
        } while (profilePage.hasNext());
    }
}
