package com.socialnetwork.post.service;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialnetwork.post.dto.CreatePostRequest;
import com.socialnetwork.post.dto.ProfileDto;
import com.socialnetwork.post.dto.StoryDto;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.post.model.Story;
import com.socialnetwork.post.repository.StoryRepository;
import com.socialnetwork.post.exception.InvalidInputException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final ProfileService profileService;
    private final UploadService uploadService;
    private final StoryRepository storyRepository;

    @Override
    @Transactional
    public StoryDto createStory(UserPrincipal userPrincipal, CreatePostRequest request) {
        ProfileDto profile = profileService.getProfile(userPrincipal);
        
        String url = uploadService.uploadImage(request.getBase64ImageString());
        if (url == null || url.isBlank()) {
            throw new InvalidInputException("Story image upload failed");
        }

        Story story = Story.builder()
                .createdByProfileId(profile.getId())
                .imageUrl(url)
                .createdAt(new Date())
                .build();
                
        story = storyRepository.save(story);
        log.info("Created story with id: {} by profileId: {}", story.getId(), profile.getId());

        return StoryDto.builder()
                .id(story.getId())
                .imageUrl(story.getImageUrl())
                .createdAt(story.getCreatedAt())
                .createdBy(profile)
                .build();
    }
}
