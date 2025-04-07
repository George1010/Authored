package com.authored.blogapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> status = new HashMap<>();

        // Check MongoDB
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            status.put("mongo", "UP");
        } catch (Exception e) {
            status.put("mongo", "DOWN");
        }

        // Check Redis
        try {
            redisTemplate.opsForValue().set("health_check_key", "OK");
            Object result = redisTemplate.opsForValue().get("health_check_key");
            status.put("redis", result != null ? "UP" : "DOWN");
        } catch (Exception e) {
            status.put("redis", "DOWN");
        }

        status.put("status", status.containsValue("DOWN") ? "DEGRADED" : "UP");
        return ResponseEntity.ok(status);
    }
}
