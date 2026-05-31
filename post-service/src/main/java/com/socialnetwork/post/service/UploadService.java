package com.socialnetwork.post.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /** Upload from a raw base64 string. Used when the image is already encoded (e.g. from a create-post request). */
    String uploadImage(String base64Content);

    /** Upload from a MultipartFile (e.g. profile picture update). */
    String uploadImage(MultipartFile file);
}
