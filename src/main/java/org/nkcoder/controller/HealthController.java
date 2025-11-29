package org.nkcoder.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response =
        Map.of(
            "status", "ok",
            "timestamp", LocalDateTime.now(),
            "service", "user-service");

    return ResponseEntity.ok(response);
  }
}
