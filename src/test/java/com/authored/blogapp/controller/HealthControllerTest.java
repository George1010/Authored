package com.authored.blogapp.controller;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.testng.Assert.*;

public class HealthControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private HealthController healthController;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
    }

    @Test
    public void shouldReturnUp_WhenMongoAndRedisAreHealthy() {
        // given
        given(mongoTemplate.executeCommand("{ ping: 1 }")).willReturn(null);
        given(valueOps.get("health_check_key")).willReturn("OK");

        // when
        ResponseEntity<Map<String, Object>> response = healthController.checkHealth();

        // then
        Map<String, Object> status = response.getBody();
        assertEquals(status.get("mongo"), "UP");
        assertEquals(status.get("redis"), "UP");
        assertEquals(status.get("status"), "UP");
    }

    @Test
    public void shouldReturnDegraded_WhenMongoIsDown() {
        // given
        willThrow(new RuntimeException("Mongo is down")).given(mongoTemplate).executeCommand("{ ping: 1 }");
        given(valueOps.get("health_check_key")).willReturn("OK");

        // when
        ResponseEntity<Map<String, Object>> response = healthController.checkHealth();

        // then
        Map<String, Object> status = response.getBody();
        assertEquals(status.get("mongo"), "DOWN");
        assertEquals(status.get("redis"), "UP");
        assertEquals(status.get("status"), "DEGRADED");
    }

    @Test
    public void shouldReturnDegraded_WhenRedisIsDown() {
        // given
        given(mongoTemplate.executeCommand("{ ping: 1 }")).willReturn(null);
        willThrow(new RuntimeException("Redis down")).given(valueOps).set(anyString(), any());

        // when
        ResponseEntity<Map<String, Object>> response = healthController.checkHealth();

        // then
        Map<String, Object> status = response.getBody();
        assertEquals(status.get("mongo"), "UP");
        assertEquals(status.get("redis"), "DOWN");
        assertEquals(status.get("status"), "DEGRADED");
    }
}
