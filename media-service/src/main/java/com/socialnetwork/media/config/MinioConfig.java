package com.socialnetwork.media.config;

import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the MinIO client bean used by the gRPC UploadImage handler.
 * Credentials are injected via environment variables in Docker deployments.
 */
@Configuration
public class MinioConfig {

    @Value("${spring.minio.endpoint}")
    private String endpoint;

    @Value("${spring.minio.access-key}")
    private String accessKey;

    @Value("${spring.minio.secret-key}")
    private String secretKey;

    @Value("${spring.minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
                
        // Ensure bucket exists
        boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            
            // Set bucket policy to public read
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
            client.setBucketPolicy(io.minio.SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
        }
        
        return client;
    }
}
