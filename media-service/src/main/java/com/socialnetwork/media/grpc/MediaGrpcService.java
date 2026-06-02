package com.socialnetwork.media.grpc;

import com.socialnetwork.grpc.media.MediaServiceGrpc;
import com.socialnetwork.grpc.media.UploadImageRequest;
import com.socialnetwork.grpc.media.UploadImageResponse;

import io.grpc.stub.StreamObserver;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * gRPC server implementation for {@link MediaServiceGrpc}.
 *
 * <p>Replaces the monolith's {@code UploadServiceImpl} bean. Other services
 * (Post, Profile) now call this via gRPC instead of injecting UploadService
 * directly — breaking the shared-library coupling.
 *
 * <p>Annotated with {@code @GrpcService} so that grpc-server-spring-boot-starter
 * registers it automatically on the configured gRPC port.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class MediaGrpcService extends MediaServiceGrpc.MediaServiceImplBase {

    private final MinioClient minioClient;

    @Value("${spring.minio.bucket:spring-boot}")
    private String bucket;

    // ── gRPC handler ─────────────────────────────────────────────────────────

    @Override
    public void uploadImage(UploadImageRequest request,
                            StreamObserver<UploadImageResponse> responseObserver) {

        log.info("Received UploadImage gRPC request");

        try {
            String base64 = request.getBase64Image();
            String extension = parseExtension(base64);
            InputStream imageStream = decodeBase64(base64);
            String fileName = UUID.randomUUID() + "." + extension;

            String contentType = "image/" + (extension.equals("jpg") ? "jpeg" : extension);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(imageStream, -1, 5_242_880) // 5 MB part size
                            .contentType(contentType)
                            .build()
            );

            log.info("Uploaded image: {}", fileName);

            responseObserver.onNext(
                    UploadImageResponse.newBuilder()
                            .setUrl(fileName)
                            .setSuccess(true)
                            .build()
            );

        } catch (Exception e) {
            log.error("Failed to upload image", e);

            responseObserver.onNext(
                    UploadImageResponse.newBuilder()
                            .setSuccess(false)
                            .setErrorMessage(e.getMessage())
                            .build()
            );
        }

        responseObserver.onCompleted();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Extracts the file extension from a base64 data URI prefix.
     * e.g. "data:image/jpeg;base64,..." → "jpeg"
     */
    private String parseExtension(String base64) {
        String[] parts = base64.split(",");
        if (parts.length < 1) return "jpg";

        return switch (parts[0]) {
            case "data:image/jpeg;base64" -> "jpeg";
            case "data:image/png;base64"  -> "png";
            case "data:image/gif;base64"  -> "gif";
            case "data:image/webp;base64" -> "webp";
            default                       -> "jpg";
        };
    }

    /**
     * Decodes the base64 payload portion into an {@link InputStream}.
     */
    private InputStream decodeBase64(String base64) {
        String[] parts = base64.split(",");
        String payload = parts.length > 1 ? parts[1] : parts[0];
        byte[] bytes = Base64.getDecoder().decode(payload);
        return new ByteArrayInputStream(bytes);
    }
}
