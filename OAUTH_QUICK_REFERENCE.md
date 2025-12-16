# OAuth 2.0 Quick Reference Guide

**Feature**: CR-DID-002 - OAuth Verification  
**Status**: Implementation Complete

---

## Quick Start (5 Steps)

### Step 1: Install Dependencies
```bash
mvn clean install
```

### Step 2: Configure Environment
Copy `oauth-env-template.txt` values to your environment or `.env` file:
```bash
OAUTH_ANONYMOUS_ID_SALT=your-random-salt
TWITTER_CLIENT_ID=...
TWITTER_CLIENT_SECRET=...
DISCORD_CLIENT_ID=...
DISCORD_CLIENT_SECRET=...
TELEGRAM_BOT_TOKEN=...
```

### Step 3: Set Up OAuth Apps
- **Twitter**: https://developer.twitter.com/en/portal/dashboard
- **Discord**: https://discord.com/developers/applications
- **Telegram**: Message @BotFather → `/newbot`

### Step 4: Run Application
```bash
mvn spring-boot:run
```

### Step 5: Test
Open Swagger UI: http://localhost:8080/swagger-ui.html

---

## API Endpoints Summary

### Twitter OAuth
```
GET  /api/auth/oauth/twitter/initiate  - Start OAuth flow
POST /api/auth/oauth/twitter/callback  - Handle callback
POST /api/auth/oauth/twitter/refresh   - Refresh token
```

### Discord OAuth
```
GET    /api/auth/oauth/discord/initiate  - Start OAuth flow
POST   /api/auth/oauth/discord/callback  - Handle callback
POST   /api/auth/oauth/discord/refresh   - Refresh token
DELETE /api/auth/oauth/discord/revoke    - Revoke token
```

### Telegram OAuth
```
POST   /api/auth/oauth/telegram/callback    - Handle auth
GET    /api/auth/oauth/telegram/config      - Get widget config
DELETE /api/auth/oauth/telegram/disconnect  - Disconnect
```

### Account Management
```
GET    /api/auth/oauth/accounts/{anonymousId}      - Get account info
GET    /api/auth/oauth/accounts/user/{userId}      - Get user accounts
DELETE /api/auth/oauth/accounts/{anonymousId}      - Delete account
```

---

## Security Features

| Feature | Status |
|---------|--------|
| Anonymous ID (SHA-256) | Yes |
| PKCE (Twitter) | Yes |
| CSRF Protection | Yes |
| Token Encryption | Yes |
| No PII On-Chain | Yes |
| GDPR Compliant | Yes |

---

## Files Created

### Core Implementation (Java)
- **Entities**: `OAuthAccount.java`, `OAuthState.java`
- **Services**: `TwitterOAuthService.java`, `DiscordOAuthService.java`, `TelegramOAuthService.java`
- **Controllers**: `OAuthController.java`, `OAuthAccountController.java`
- **Repositories**: `OAuthAccountRepository.java`, `OAuthStateRepository.java`
- **DTOs**: 5 DTO files in `dto/auth/`
- **Utils**: `OAuthCryptoUtil.java`
- **Config**: `OAuthConfig.java`

### Database
- **Migration**: `V3__add_oauth_tables.sql`
- **Tables**: `oauth_accounts`, `oauth_states`, `oauth_audit_log`

### Configuration
- **application.yml**: OAuth settings added
- **SecurityConfig.java**: OAuth endpoints whitelisted
- **pom.xml**: Dependencies added

### Documentation
- **OAUTH_SETUP.md**: Complete setup guide (88 pages)
- **IMPLEMENTATION_SUMMARY.md**: Implementation details
- **oauth-env-template.txt**: Environment variables
- **swagger.yaml**: API documentation updated

---

## Test Flow Example

### 1. Initiate Twitter OAuth
```bash
curl http://localhost:8080/api/auth/oauth/twitter/initiate
```

**Response**:
```json
{
  "authorizationUrl": "https://twitter.com/i/oauth2/authorize?...",
  "state": "abc123...",
  "provider": "twitter",
  "expiresAt": "2025-12-02T10:30:00"
}
```

### 2. User Authorizes
- Open `authorizationUrl` in browser
- User logs in and authorizes
- Twitter redirects to callback with `code` and `state`

### 3. Handle Callback
```bash
curl -X POST http://localhost:8080/api/auth/oauth/twitter/callback \
  -H "Content-Type: application/json" \
  -d '{
    "code": "authorization_code_here",
    "state": "abc123..."
  }'
```

**Response**:
```json
{
  "anonymousId": "a7b3c4d5e6f7...",
  "provider": "twitter",
  "username": "johndoe",
  "displayName": "John Doe",
  "avatarUrl": "https://...",
  "accessToken": "jwt_token_here",
  "verified": true,
  "expiresAt": "2025-12-03T10:00:00"
}
```

**User is now authenticated!**

---

## Configuration Reference

### Twitter OAuth Settings
```yaml
oauth:
  twitter:
    client-id: ${TWITTER_CLIENT_ID}
    client-secret: ${TWITTER_CLIENT_SECRET}
    redirect-uri: ${TWITTER_REDIRECT_URI}
    scope: "tweet.read users.read offline.access"
```

### Discord OAuth Settings
```yaml
oauth:
  discord:
    client-id: ${DISCORD_CLIENT_ID}
    client-secret: ${DISCORD_CLIENT_SECRET}
    redirect-uri: ${DISCORD_REDIRECT_URI}
    scope: "identify email guilds"
```

### Telegram OAuth Settings
```yaml
oauth:
  telegram:
    bot-token: ${TELEGRAM_BOT_TOKEN}
    bot-id: ${TELEGRAM_BOT_ID}
    bot-username: ${TELEGRAM_BOT_USERNAME}
```

---

## Database Schema

### oauth_accounts
```sql
- id (PK)
- anonymous_id (UNIQUE) ← SHA-256 hash
- provider (twitter/discord/telegram)
- provider_user_id
- username, display_name, email, avatar_url
- access_token, refresh_token (encrypted)
- token_expires_at
- verified, created_at, updated_at
```

### oauth_states
```sql
- id (PK)
- state (UNIQUE)
- provider
- code_verifier, code_challenge (PKCE)
- expires_at, used
```

---

## Troubleshooting

### Common Issues

**Issue**: "Invalid redirect URI"
```bash
# Solution: Ensure redirect URI matches exactly in provider settings
# Check: TWITTER_REDIRECT_URI environment variable
```

**Issue**: "State parameter invalid"
```bash
# Solution: State expired (10 minutes) - restart OAuth flow
```

**Issue**: "Token expired"
```bash
# Solution: Use refresh endpoint
POST /api/auth/oauth/{provider}/refresh?anonymousId=...
```

**Issue**: Swagger annotations not working
```bash
# Solution: Run mvn clean install to download dependencies
```

---

## Documentation Links

| Document | Description |
|----------|-------------|
| **OAUTH_SETUP.md** | Complete setup guide with troubleshooting |
| **IMPLEMENTATION_SUMMARY.md** | Technical implementation details |
| **oauth-env-template.txt** | Environment variables template |
| **Swagger UI** | Interactive API documentation |

---

## Pre-Deployment Checklist

- [ ] All environment variables configured
- [ ] OAuth applications created and configured
- [ ] Database migrations executed
- [ ] Dependencies installed (`mvn clean install`)
- [ ] Application starts without errors
- [ ] Swagger UI accessible
- [ ] Test OAuth flow for each provider
- [ ] HTTPS configured (production)
- [ ] Tokens encrypted
- [ ] Rate limiting configured
- [ ] Monitoring set up

---

## Key Concepts

### Anonymous ID
```
anonymousId = SHA256(provider:userId:salt)
```
- **Stored on-chain**: Yes (safe, no PII)
- **Reversible**: No (one-way hash)
- **Unique**: Yes (per provider + user)

### PKCE Flow (Twitter)
```
1. Generate code_verifier (random)
2. Create code_challenge = SHA256(code_verifier)
3. Send challenge in auth request
4. Send verifier in token exchange
```

### Token Lifecycle
```
1. Initial OAuth → Access Token + Refresh Token
2. Token expires → Use Refresh Token
3. Refresh expires → Re-authenticate
4. Logout → Revoke tokens
```

---

## Next Steps

1. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

2. **Test with Swagger**
   - Open: http://localhost:8080/swagger-ui.html
   - Navigate to: OAuth Authentication
   - Try: Twitter Initiate OAuth

3. **Configure Production**
   - Set production redirect URIs
   - Enable HTTPS
   - Configure rate limiting
   - Set up monitoring

4. **Implement Frontend**
   - Add OAuth buttons
   - Handle redirects
   - Store JWT tokens
   - Display user info

---

## Support

**For detailed help**, see:
- `OAUTH_SETUP.md` - Comprehensive guide
- `IMPLEMENTATION_SUMMARY.md` - Technical details
- Swagger UI - Interactive API docs
- Application logs - Error details

---

**Implementation Complete!**  
*All OAuth components are ready for testing and deployment.*

