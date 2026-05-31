package com.socialnetwork.post.grpc;

import com.socialnetwork.grpc.media.MediaServiceGrpc;
import com.socialnetwork.grpc.media.UploadImageRequest;
import com.socialnetwork.grpc.media.UploadImageResponse;
import com.socialnetwork.post.service.UploadService;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * gRPC-based implementation of {@link UploadService}.
 *
 * <p>This replaces the old {@code UploadServiceImpl} that called MinIO directly.
 * Instead, it delegates the upload work to the standalone <b>media-service</b>
 * via a gRPC call — enforcing service boundary isolation.
 *
 * <p>The {@code @GrpcClient("media-service")} annotation instructs
 * grpc-client-spring-boot-starter to inject a managed channel configured
 * under {@code grpc.client.media-service.*} in application properties.
 */
@Slf4j
@Service("uploadService")
public class MediaServiceGrpcClient implements UploadService {

    @GrpcClient("media-service")
    private MediaServiceGrpc.MediaServiceBlockingStub mediaServiceStub;

    /**
     * Delegates the base64 image upload to the media-service via gRPC.
     *
     * @param base64 full base64 data URI (e.g. "data:image/jpeg;base64,...")
     * @return the stored object name / URL, or {@code null} on failure
     */
    @Override
    public String uploadImage(String base64) {
        log.info("Calling media-service gRPC UploadImage");

        try {
            UploadImageRequest request = UploadImageRequest.newBuilder()
                    .setBase64Image(base64)
                    .build();

            UploadImageResponse response = mediaServiceStub.uploadImage(request);

            if (response.getSuccess()) {
                log.info("Image uploaded successfully, url={}", response.getUrl());
                return response.getUrl();
            } else {
                log.error("Media service reported upload failure: {}", response.getErrorMessage());
                return null;
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC call to media-service failed: status={}, message={}",
                    e.getStatus().getCode(), e.getMessage());
            return null;
        }
    }
}
