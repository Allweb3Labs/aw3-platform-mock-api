package com.aw3.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for AW3 Platform Backend
 * 
 * Portal-Oriented Architecture:
 * - Creator Portal: /api/creator/*
 * - Project Portal: /api/project/*
 * - Validator Portal: /api/validator/*
 * - Admin Portal: /api/admin/*
 * - Shared/Public: /api/auth/*, /api/marketplace/*, /api/governance/*
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class AW3PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AW3PlatformApplication.class, args);
    }
}

