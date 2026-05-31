package com.socialnetwork.post.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    String uploadImage(MultipartFile file);
    String uploadImage(String base64Content);
}
