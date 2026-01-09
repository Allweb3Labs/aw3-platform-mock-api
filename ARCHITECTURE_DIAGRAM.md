# AW3 Platform - Authentication & Social Verification Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                            USER / FRONTEND                               │
└────────────┬────────────────────────────────────────────┬────────────────┘
             │                                            │
             │                                            │
             ▼                                            ▼
┌────────────────────────────────┐      ┌────────────────────────────────┐
│   PRIVY AUTHENTICATION         │      │  SOCIAL VERIFICATION SERVICE   │
│   (Platform Access Only)        │      │  (Independent Service)         │
├────────────────────────────────┤      ├────────────────────────────────┤
│                                │      │                                │
│ Purpose:                       │      │ Purpose:                       │
│ • User login to AW3 platform   │      │ • Verify social accounts       │
│                                │      │ • Fetch social metrics         │
│ Methods:                       │      │                                │
│ • Gmail (via Privy)            │      │ Platforms:                     │
│ • Twitter (via Privy)          │      │ • Twitter/X                    │
│ • Wallet (MetaMask, etc.)      │      │ • Discord                      │
│                                │      │ • Telegram                     │
│ Results:                       │      │ • Instagram                    │
│ • JWT access token             │      │ • YouTube                      │
│ • JWT refresh token            │      │ • TikTok                       │
│ • User session                 │      │                                │
│                                │      │ Process:                       │
│ Endpoints:                     │      │ • Own OAuth 2.0 flows          │
│ • POST /auth/wallet-connect    │      │ • Direct platform API calls    │
│ • POST /auth/verify-signature  │      │ • Fetch followers, engagement  │
│ • POST /auth/register          │      │ • Store metrics (no tokens)    │
│ • POST /auth/refresh           │      │                                │
│ • POST /auth/logout            │      │ Results:                       │
│ • GET  /auth/nonce/{address}   │      │ • Verified social accounts     │
│                                │      │ • Follower counts              │
│                                │      │ • Engagement metrics           │
│                                │      │ • Verification timestamp       │
│                                │      │                                │
│                                │      │ Endpoints:                     │
│                                │      │ • POST /creator/profile/       │
│                                │      │        social-verification     │
│                                │      │ • GET  /social/{platform}/     │
│                                │      │        authorize-url           │
│                                │      │ • GET  /social/callback        │
│                                │      │ • GET  /social/status          │
│                                │      │ • DELETE /social/{platform}/   │
│                                │      │          disconnect            │
└────────────┬───────────────────┘      └────────────┬───────────────────┘
             │                                       │
             │                                       │
             ▼                                       ▼
┌────────────────────────────────┐      ┌────────────────────────────────┐
│      PRIVY SERVICE             │      │  PLATFORM APIs                 │
│      (Third-party)             │      │  (Twitter, Discord, etc.)      │
└────────────────────────────────┘      └────────────────────────────────┘

     ═══════════════════════════              ═══════════════════════════
     NO CONNECTION BETWEEN THESE              COMPLETELY INDEPENDENT
     ═══════════════════════════              ═══════════════════════════
```

---

## Flow Diagrams

### Flow 1: Platform Authentication (via Privy)

```
User                Frontend             AW3 Backend            Privy Service
 │                      │                     │                      │
 │  Click "Login"       │                     │                      │
 │─────────────────────>│                     │                      │
 │                      │                     │                      │
 │                      │  Privy SDK: login() │                      │
 │                      │─────────────────────────────────────────> │
 │                      │                     │                      │
 │  <──── Privy Login Modal ──────────────────────────────────────> │
 │  (Gmail / Twitter / Wallet)               │                      │
 │                      │                     │                      │
 │  User authenticates  │                     │                      │
 │─────────────────────────────────────────────────────────────────>│
 │                      │                     │                      │
 │                      │<─ Privy Token ──────────────────────────── │
 │                      │                     │                      │
 │                      │  POST /auth/verify  │                      │
 │                      │────────────────────>│                      │
 │                      │  (privy token)      │                      │
 │                      │                     │                      │
 │                      │  <- AW3 JWT Token ──│                      │
 │<─────────────────────│                     │                      │
 │  (Access granted to  │                     │                      │
 │   AW3 platform)      │                     │                      │
```

**Result:** User can now access AW3 platform APIs

---

### Flow 2: Social Verification (Independent Service)

```
User            Frontend        Social Verification     Platform
                                Backend Service         (Twitter/etc)
 │                  │                  │                     │
 │  Click "Verify   │                  │                     │
 │  Twitter"        │                  │                     │
 │─────────────────>│                  │                     │
 │                  │                  │                     │
 │                  │  GET /social/    │                     │
 │                  │  twitter/        │                     │
 │                  │  authorize-url   │                     │
 │                  │─────────────────>│                     │
 │                  │                  │                     │
 │                  │<─ OAuth URL ─────│                     │
 │                  │                  │                     │
 │                  │  Redirect to URL │                     │
 │                  │─────────────────────────────────────> │
 │                  │                  │                     │
 │  <──── Twitter Authorization Screen ────────────────────>│
 │  (User grants access)               │                     │
 │                  │                  │                     │
 │                  │                  │<─ Callback with code│
 │                  │                  │     & state ────────│
 │                  │                  │                     │
 │                  │                  │  Exchange code      │
 │                  │                  │  for token ────────>│
 │                  │                  │                     │
 │                  │                  │<─ Access Token ─────│
 │                  │                  │                     │
 │                  │                  │  Fetch user profile │
 │                  │                  │  & metrics ────────>│
 │                  │                  │                     │
 │                  │                  │<─ Followers, etc ───│
 │                  │                  │                     │
 │                  │<─ Redirect to ───│                     │
 │                  │   AW3 success    │                     │
 │<─────────────────│   page           │                     │
 │  (Twitter verified,│                │                     │
 │   125K followers)  │                │                     │
 │                  │                  │                     │
 │                  │  [Token discarded│                     │
 │                  │   in MVP]        │                     │
```

**Result:** Twitter account verified with follower metrics stored

---

## Example User Journey

### Scenario: Creator signing up and verifying social accounts

```
Step 1: Platform Authentication (Privy)
├── User visits AW3 platform
├── Clicks "Sign Up"
├── Chooses "Login with Gmail" (via Privy)
├── Authenticates with Google
├── AW3 issues JWT token
└── User now logged into AW3 platform ✓

     ↓ (User can now access AW3 features)

Step 2: Social Verification (Independent Service)
├── User navigates to Profile > Social Accounts
├── Clicks "Verify Twitter"
├── Redirected to Twitter.com
├── Authorizes AW3 to read public profile
├── Redirected back to AW3
├── Backend fetches Twitter metrics:
│   ├── @cryptoinfluencer
│   ├── 125,000 followers
│   └── 8.5% engagement rate
└── Twitter account verified ✓

     ↓ (Optional: Verify more platforms)

Step 3: Verify Additional Platforms (Independent)
├── User clicks "Verify Discord"
├── Same OAuth flow with Discord
├── Verifies ownership of Discord server
├── Fetches member count
└── Discord verified ✓

Step 4: Apply to Campaigns
├── User browses campaigns
├── Campaign requires: 50K+ Twitter followers ✓
├── Campaign prefers: Discord community ✓
├── User applies successfully
└── Project sees verified metrics in application
```

---

## Key Differences: Privy Login vs Social Verification

| Aspect | Privy Login (Twitter) | Social Verification (Twitter) |
|--------|----------------------|------------------------------|
| **Purpose** | Log into AW3 platform | Verify Twitter for metrics |
| **OAuth Provider** | Privy | Independent service (direct Twitter API) |
| **What it proves** | User owns Twitter account | User owns Twitter + fetch metrics |
| **Token stored** | Privy manages (hidden) | No tokens stored (MVP) |
| **Data fetched** | Basic identity | Followers, engagement, tweets |
| **Use case** | Platform access | Campaign eligibility |
| **Can be different accounts** | Yes! | Yes! |

### Example Scenarios

#### Scenario A: Gmail login, Twitter verification
```
Login Method:    Gmail (via Privy)           ← Platform authentication
Social Verified: Twitter (@cryptoinfluencer) ← Social metrics
Result:          Different accounts, no conflict ✓
```

#### Scenario B: Twitter login, Twitter verification
```
Login Method:    Twitter (via Privy)         ← Platform authentication
Social Verified: Twitter (@cryptoinfluencer) ← Social metrics
Result:          STILL TWO SEPARATE FLOWS - both needed! ✓
Note:            Even though both use Twitter, these are different:
                 - Privy Twitter login = Proves identity to AW3
                 - Social verification = Fetches metrics for campaigns
```

#### Scenario C: Wallet login, Multiple social verifications
```
Login Method:    Wallet (MetaMask)           ← Platform authentication
Social Verified: Twitter (@user1)            ← Social metrics
                 Discord (Server: 5000)       ← Social metrics
                 Telegram (@channel)          ← Social metrics
Result:          One login method, multiple social accounts ✓
```

---

## Data Flow

### What Privy Stores
```
User ID: privy-user-12345
Linked Accounts:
  - type: google
    email: user@gmail.com
  OR
  - type: twitter_oauth
    username: @someuser
  OR
  - type: wallet
    address: 0x742d35Cc6634C0532925a3b844Bc454e4438f44e

AW3 receives:
  - Privy user ID
  - Authentication confirmation
  - Basic profile info
```

### What Social Verification Service Stores
```
Creator ID: aw3-creator-789
Verified Accounts:
  - platform: twitter
    handle: @cryptoinfluencer
    platform_user_id_hash: sha256(twitter:123456789:salt)
    followers_count: 125000
    engagement_rate: 8.5
    verified_at: 2025-12-15T08:30:00Z
    
  - platform: discord
    handle: CryptoInfluencer#1337
    platform_user_id_hash: sha256(discord:987654321:salt)
    guild_members: 5000
    verified_at: 2025-12-15T09:15:00Z

NOT stored:
  - OAuth access tokens (discarded after fetch)
  - OAuth refresh tokens (MVP only)
  - Private messages
  - Email addresses
  - Personal information
```

---

## Security Boundaries

```
┌──────────────────────────────────────────────────────────────┐
│  PRIVY SECURITY BOUNDARY                                     │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  • User identity verification                          │  │
│  │  • Session management                                  │  │
│  │  │  │  • Token issuance                                         │  │
│  │  • Email/Social/Wallet authentication                  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  AW3 trusts Privy for:                                       │
│  ✓ User is who they claim to be                             │
│  ✓ Authentication is secure                                 │
│  ✓ Session management                                       │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  SOCIAL VERIFICATION SECURITY BOUNDARY                       │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  • OAuth 2.0 implementation (own code)                 │  │
│  │  • State parameter validation (CSRF protection)        │  │
│  │  • PKCE code verifier validation                       │  │
│  │  • Token exchange (server-side only)                   │  │
│  │  • Platform API calls                                  │  │
│  │  • Metric fetching and validation                      │  │
│  │  • Data hashing for privacy                            │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  Social verification handles:                                │
│  ✓ User owns social account                                 │
│  ✓ Metrics are accurate                                     │
│  ✓ Data privacy (hashing, no PII)                           │
│  ✓ Token security (discarded after use)                     │
└──────────────────────────────────────────────────────────────┘

     ╔═══════════════════════════════════════════════════╗
     ║  NO SHARED SECRETS OR TOKENS BETWEEN BOUNDARIES   ║
     ╚═══════════════════════════════════════════════════╝
```

---

## API Endpoint Organization

```
/auth/*
  ├── /auth/wallet-connect         ─┐
  ├── /auth/verify-signature        │
  ├── /auth/register                ├─ Privy Platform Authentication
  ├── /auth/refresh                 │
  ├── /auth/logout                  │
  └── /auth/nonce/{address}        ─┘

/creator/profile/social-verification ─┐
                                      │
/social/*                             │
  ├── /social/{platform}/authorize-url│
  ├── /social/callback                ├─ Independent Social Verification
  ├── /social/status                  │
  └── /social/{platform}/disconnect  ─┘
```

---

## Summary

| Aspect | Privy Auth | Social Verification |
|--------|-----------|---------------------|
| **Provider** | Privy (third-party) | AW3 (own service) |
| **OAuth Implementation** | Privy handles | AW3 implements |
| **Purpose** | Platform access | Social metrics |
| **Token Management** | Privy manages | AW3 manages (or discards in MVP) |
| **Data Stored** | User identity | Social metrics |
| **Dependency** | None on social verification | None on Privy |
| **Can operate independently** | Yes ✓ | Yes ✓ |

**Bottom Line:** Two completely separate systems with zero dependency between them.
