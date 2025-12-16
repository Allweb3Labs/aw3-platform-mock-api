package com.aw3.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OAuth providers
 */
@Configuration
@ConfigurationProperties(prefix = "oauth")
@Data
public class OAuthConfig {
    
    private String anonymousIdSalt;
    private Twitter twitter = new Twitter();
    private Discord discord = new Discord();
    private Telegram telegram = new Telegram();
    
    @Data
    public static class Twitter {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope = "tweet.read users.read offline.access";
        private String authorizationUrl = "https://twitter.com/i/oauth2/authorize";
        private String tokenUrl = "https://api.twitter.com/2/oauth2/token";
        private String userInfoUrl = "https://api.twitter.com/2/users/me";
    }
    
    @Data
    public static class Discord {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope = "identify email guilds";
        private String authorizationUrl = "https://discord.com/oauth2/authorize";
        private String tokenUrl = "https://discord.com/api/oauth2/token";
        private String userInfoUrl = "https://discord.com/api/users/@me";
        private String tokenRevocationUrl = "https://discord.com/api/oauth2/token/revoke";
    }
    
    @Data
    public static class Telegram {
        private String botToken;
        private String botId;
        private String botUsername;
        private Integer authExpirySeconds = 86400; // 24 hours
    }
}

