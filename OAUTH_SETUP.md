# OAuth 2.0 Authentication Setup Guide

This guide provides step-by-step instructions for setting up OAuth 2.0 authentication with Twitter, Discord, and Telegram for the AW3 Platform Creator Portal.

**Feature ID**: CR-DID-002  
**Status**: Backend Implementation Complete  
**Date**: December 2, 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Privacy & Security](#privacy--security)
4. [Twitter OAuth Setup](#twitter-oauth-setup)
5. [Discord OAuth Setup](#discord-oauth-setup)
6. [Telegram OAuth Setup](#telegram-oauth-setup)
7. [Environment Configuration](#environment-configuration)
8. [API Endpoints](#api-endpoints)
9. [Testing](#testing)
10. [Troubleshooting](#troubleshooting)

---

## Overview

This implementation provides OAuth 2.0 authentication for three social platforms:

- **Twitter**: OAuth 2.0 with PKCE (Proof Key for Code Exchange)
- **Discord**: OAuth 2.0 Authorization Code Flow
- **Telegram**: Login Widget Authentication

### Key Features

- Privacy-protected anonymous mapping (SHA-256 hashing)
- No personal data stored on-chain
- Automatic token refresh
- CSRF protection with state parameter
- Secure token storage
- GDPR compliant

---

## Architecture

### Authentication Flow

1. User initiates OAuth flow from Creator Portal
2. User is redirected to social platform for authorization
3. Platform returns authorization code to callback URL
4. Backend exchanges code for access token
5. Backend retrieves minimal user information
6. System creates anonymous mapping (hash-based identifier)
7. User session is established without storing personal data on-chain

### Data Storage

**Backend Database** (MySQL):
- OAuth tokens (encrypted)
- Provider user IDs
- Usernames and display names
- Email addresses (if provided)
- Avatar URLs

**On-Chain** (Smart Contract):
- **ONLY** anonymous identifiers (SHA-256 hashes)
- No personal identifiable information (PII)

---

## Privacy & Security

### Anonymous ID Generation

Anonymous IDs are generated using SHA-256:

```
anonymousId = SHA256(provider:userId:salt)
```

Example:
- Input: `twitter:123456789:random-salt`
- Output: `a7b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0d1e2f3`

**Properties:**
- Deterministic (same user = same ID)
- One-way (cannot reverse to get original ID)
- Unique per provider
- Safe for on-chain storage

### Security Measures

1. **PKCE Implementation** (Twitter)
   - Code verifier and challenge
   - Protection against authorization code interception

2. **State Parameter** (All providers)
   - CSRF attack prevention
   - State expires after 10 minutes

3. **Token Management**
   - Tokens stored encrypted
   - Automatic token refresh
   - Secure token revocation

4. **HTTPS Enforcement**
   - All OAuth flows require HTTPS in production
   - SSL certificate validation

---

## Twitter OAuth Setup

### Prerequisites

1. Twitter Developer Account
2. Twitter App created in Developer Portal

### Step-by-Step Setup

#### 1. Create Twitter Application

1. Visit [Twitter Developer Portal](https://developer.twitter.com/en/portal/dashboard)
2. Click "Create Project" or select existing project
3. Create a new App within the project
4. Note down your **Client ID** and **Client Secret**

#### 2. Configure OAuth 2.0

1. In your app settings, navigate to "User authentication settings"
2. Click "Set up" for OAuth 2.0
3. Enable **OAuth 2.0** with PKCE
4. Set **Type of App**: Web App
5. Set **Callback URI**: 
   ```
   https://yourdomain.com/api/auth/oauth/twitter/callback
   ```
   (Use `http://localhost:8080` for local development)

#### 3. Configure Scopes

Select the following scopes:
- `tweet.read` - Read tweets
- `users.read` - Read user profile information
- `offline.access` - Refresh token capability (optional)

#### 4. Set Environment Variables

Add to your `.env` file:

```bash
TWITTER_CLIENT_ID=your_client_id_here
TWITTER_CLIENT_SECRET=your_client_secret_here
TWITTER_REDIRECT_URI=https://yourdomain.com/api/auth/oauth/twitter/callback
```

### API Rate Limits

- User lookup: 300 requests per 15 minutes
- Token exchange: 1,500 requests per 15 minutes

---

## Discord OAuth Setup

### Prerequisites

1. Discord Account
2. Discord Developer Application

### Step-by-Step Setup

#### 1. Create Discord Application

1. Visit [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application"
3. Give your application a name
4. Navigate to "OAuth2" section
5. Note down your **Client ID** and **Client Secret**

#### 2. Configure OAuth2

1. In OAuth2 section, find "Redirects"
2. Click "Add Redirect"
3. Add your callback URL:
   ```
   https://yourdomain.com/api/auth/oauth/discord/callback
   ```
   **Important**: URL must match exactly (including trailing slashes)

#### 3. Configure Scopes

Available scopes:
- `identify` - Basic user information (recommended)
- `email` - User's email address
- `guilds` - Basic information about user's guilds (optional)

#### 4. Set Environment Variables

Add to your `.env` file:

```bash
DISCORD_CLIENT_ID=your_client_id_here
DISCORD_CLIENT_SECRET=your_client_secret_here
DISCORD_REDIRECT_URI=https://yourdomain.com/api/auth/oauth/discord/callback
```

### Important Notes

- Access tokens expire after **7 days** (604,800 seconds)
- Always use `Content-Type: application/x-www-form-urlencoded` for token requests
- Discriminators are being phased out (new users have "0")
- Animated avatars have hash starting with "a_" (use .gif format)

---

## Telegram OAuth Setup

### Prerequisites

1. Telegram Account
2. Access to @BotFather

### Step-by-Step Setup

#### 1. Create Telegram Bot

1. Open Telegram and search for `@BotFather`
2. Send `/newbot` command
3. Follow the instructions:
   - Provide bot name (displayed name)
   - Provide bot username (must end with "bot")
4. Save the **Bot Token** provided by BotFather

#### 2. Configure Bot Domain

1. Send `/setdomain` to @BotFather
2. Select your bot
3. Enter your domain: `yourdomain.com`

#### 3. Set Environment Variables

Add to your `.env` file:

```bash
TELEGRAM_BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_BOT_ID=1234567890
TELEGRAM_BOT_USERNAME=your_bot_username
```

### Frontend Integration

Add Telegram Login Widget to your HTML:

```html
<script async src="https://telegram.org/js/telegram-widget.js?22"
  data-telegram-login="YOUR_BOT_USERNAME"
  data-size="large"
  data-auth-url="https://yourdomain.com/api/auth/oauth/telegram/callback"
  data-request-access="write">
</script>
```

### Important Notes

- Authentication data expires after **24 hours**
- No OAuth tokens are provided (widget-based authentication)
- Hash verification uses HMAC-SHA256
- Bot token must be kept secret

---

## Environment Configuration

### Complete .env File

```bash
# OAuth Anonymous ID Salt
OAUTH_ANONYMOUS_ID_SALT=generate-a-random-salt-string-here

# Twitter OAuth
TWITTER_CLIENT_ID=your_client_id
TWITTER_CLIENT_SECRET=your_client_secret
TWITTER_REDIRECT_URI=https://yourdomain.com/api/auth/oauth/twitter/callback

# Discord OAuth
DISCORD_CLIENT_ID=your_client_id
DISCORD_CLIENT_SECRET=your_client_secret
DISCORD_REDIRECT_URI=https://yourdomain.com/api/auth/oauth/discord/callback

# Telegram OAuth
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_BOT_ID=your_bot_id
TELEGRAM_BOT_USERNAME=your_bot_username
```

### Generating Secure Salt

Generate a random salt for anonymous ID generation:

```bash
# Linux/Mac
openssl rand -hex 32

# Or use online generator
# https://www.random.org/strings/
```

---

## API Endpoints

### Twitter OAuth

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/oauth/twitter/initiate` | Start Twitter OAuth flow |
| POST | `/api/auth/oauth/twitter/callback` | Handle Twitter callback |
| POST | `/api/auth/oauth/twitter/refresh` | Refresh access token |

### Discord OAuth

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/oauth/discord/initiate` | Start Discord OAuth flow |
| POST | `/api/auth/oauth/discord/callback` | Handle Discord callback |
| POST | `/api/auth/oauth/discord/refresh` | Refresh access token |
| DELETE | `/api/auth/oauth/discord/revoke` | Revoke token and disconnect |

### Telegram OAuth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/oauth/telegram/callback` | Handle Telegram auth |
| GET | `/api/auth/oauth/telegram/config` | Get widget configuration |
| DELETE | `/api/auth/oauth/telegram/disconnect` | Disconnect account |

### Account Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/oauth/accounts/{anonymousId}` | Get account info |
| GET | `/api/auth/oauth/accounts/user/{userId}` | Get user's OAuth accounts |
| DELETE | `/api/auth/oauth/accounts/{anonymousId}` | Delete OAuth account |

---

## Testing

### Test OAuth Flow Locally

1. **Start the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Initiate OAuth** (Twitter example):
   ```bash
   curl http://localhost:8080/api/auth/oauth/twitter/initiate
   ```

3. **Response**:
   ```json
   {
     "authorizationUrl": "https://twitter.com/i/oauth2/authorize?...",
     "state": "a1b2c3d4...",
     "provider": "twitter",
     "expiresAt": "2025-12-02T10:30:00"
   }
   ```

4. **Open the authorization URL** in a browser

5. **Authorize the application** on Twitter/Discord/Telegram

6. **Callback will be received** with code and state

### Test with Postman

Import the OAuth endpoints into Postman:

1. Create a new request collection
2. Add environment variables for tokens
3. Test each endpoint sequentially

---

## Troubleshooting

### Common Issues

#### Twitter OAuth

**Issue**: "Invalid redirect URI"
- **Solution**: Ensure redirect URI in code matches exactly with Twitter Developer Portal configuration

**Issue**: "PKCE verification failed"
- **Solution**: Verify code verifier is correctly stored in session and passed to token endpoint

**Issue**: "Token expired"
- **Solution**: Implement token refresh logic using refresh_token

#### Discord OAuth

**Issue**: "Invalid OAuth2 state"
- **Solution**: Ensure state parameter is validated correctly on callback

**Issue**: "Missing access"
- **Solution**: Check that required scopes are configured in Discord application

**Issue**: "Rate limited"
- **Solution**: Implement exponential backoff and respect rate limit headers

#### Telegram OAuth

**Issue**: "Hash verification failed"
- **Solution**: Ensure bot token is correct and hash calculation follows Telegram specification

**Issue**: "Authentication data expired"
- **Solution**: Check auth_date timestamp and reduce time window if necessary

**Issue**: "Bot not configured"
- **Solution**: Verify bot is created via @BotFather and domain is set correctly

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.aw3.platform.service: DEBUG
    com.aw3.platform.controller.auth: DEBUG
```

---

## Production Checklist

Before deploying to production:

- [ ] All OAuth applications configured in production environment
- [ ] Client IDs and secrets stored in secure environment variables
- [ ] Database migrations executed successfully
- [ ] HTTPS enabled and certificates valid
- [ ] Callback URLs configured correctly for production domain
- [ ] CORS policies configured for production frontend
- [ ] Rate limiting implemented
- [ ] Error handling tested
- [ ] Logging configured
- [ ] Security audit completed
- [ ] Test all OAuth flows in production
- [ ] Monitor error rates
- [ ] Verify anonymous ID generation
- [ ] Confirm no PII in on-chain data

---

## Support

For issues or questions:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review API documentation at `/swagger-ui.html`
3. Check application logs for error details
4. Refer to official OAuth documentation:
   - [Twitter OAuth 2.0](https://developer.twitter.com/en/docs/authentication/oauth-2-0)
   - [Discord OAuth 2.0](https://discord.com/developers/docs/topics/oauth2)
   - [Telegram Login Widget](https://core.telegram.org/widgets/login)

---

## License

© 2025 AW3 Platform. All rights reserved.

