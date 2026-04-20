package com.socialnetwork.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.dto.UploadImageRequest;
import com.socialnetwork.dto.UploadImageResponse;
import com.socialnetwork.service.UploadService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/upload")
public class UploadController {
    @Autowired
    UploadService uploadService;

    @PostMapping()
    public ResponseEntity<UploadImageResponse> postMethodName(@Valid @RequestBody UploadImageRequest request) {
        String url = uploadService.uploadImage(request.getBase64ImageString());
        return ResponseEntity.status(HttpStatus.OK).body(UploadImageResponse.builder().url(url).build());
    }
}
