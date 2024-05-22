package com.example.assettracking.service.Impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.example.assettracking.service.BL.CustomCacheServiceBL;

import java.util.concurrent.TimeUnit;

@Service
public class CustomCacheServiceImpl implements CustomCacheServiceBL{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void put(String key, Object value, long ttl, TimeUnit unit) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, ttl, unit);
    }

    public Object get(String key) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
