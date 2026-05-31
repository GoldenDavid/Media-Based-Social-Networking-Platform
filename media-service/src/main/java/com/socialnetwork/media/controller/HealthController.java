package com.socialnetwork.media.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple liveness endpoint for Docker health-checks and load-balancer probes.
 * The Actuator /health endpoint is also available via the management config.
 */
@RestController
@RequestMapping("/")
public class HealthController {

    @GetMapping("health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "media-service",
                "status", "UP"
        ));
    }
}
