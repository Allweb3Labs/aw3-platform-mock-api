package com.aw3.platform.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration
 * 
 * Cache Strategy (from Tech Stack documentation):
 * - User Profiles: 5 minutes TTL
 * - Campaign Listings: 1 minute TTL
 * - CVPI Scores: 24 hours TTL
 * - Platform Statistics: 30 minutes TTL
 * - Reputation Scores: 15 minutes TTL
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Specific cache configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User profiles: 5 minutes
        cacheConfigurations.put("userProfiles", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // Campaign listings: 1 minute
        cacheConfigurations.put("campaignListings", 
                defaultConfig.entryTtl(Duration.ofMinutes(1)));
        
        // CVPI scores: 24 hours
        cacheConfigurations.put("cvpiScores", 
                defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Platform statistics: 30 minutes
        cacheConfigurations.put("platformStats", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Reputation scores: 15 minutes
        cacheConfigurations.put("reputationScores", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Fee estimates: 15 minutes (valid for 15 minutes)
        cacheConfigurations.put("feeEstimates", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}

