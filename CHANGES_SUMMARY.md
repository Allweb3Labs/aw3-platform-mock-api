# API Endpoint Changes Summary - Authentication Architecture Clarification

**Date:** December 16, 2025  
**Purpose:** Clarify the separation between Privy authentication (AW3 platform access) and Social Verification (independent service)

---

## Overview

Based on the analysis in `Social_Verification_Analysis.txt` and Privy documentation review, the API specification has been updated to clearly reflect the correct architecture:

### Correct Architecture

1. **Authentication (Privy)**: Users log into AW3 platform using Privy (Gmail, Twitter social login, or Wallet)
2. **Social Verification (Independent Service)**: Completely separate service that handles OAuth to social platforms to verify accounts and fetch metrics

**Key Point:** Privy is ONLY for AW3 platform authentication, NOT for social verification. Social verification is handled by a completely independent backend service with its own OAuth implementation.

---

## Changes Made

### 1. API Description (info section)

**Updated:** Main API description to include comprehensive authentication architecture explanation

**Added:**
- Section "Authentication Architecture" with two subsystems clearly explained
- Platform Authentication (Privy) details
- Social Verification (Independent Service) details
- Key distinction examples showing independence between the two systems

**Example distinction provided:**
- A user can log into AW3 with Gmail but verify their Twitter account for metrics
- A user can log into AW3 with Twitter (via Privy) but STILL needs separate Twitter verification for social metrics

---

### 2. Auth Tag Description

**Updated:** From simple one-liner to comprehensive architecture note

**Now includes:**
- Clear statement that Privy is ONLY for AW3 platform authentication
- Supported authentication methods (Gmail, Twitter via Privy, Wallet)
- JWT token issuance for platform access
- Explicit separation of social verification with bullet points:
  - Independent backend service (NOT Privy)
  - Own OAuth implementation
  - Direct platform API access
  - No dependency between Privy auth and social verification

---

### 3. Authentication Endpoints Updates

#### `/auth/wallet-connect` (POST)

**Summary updated to:** "Initiate AW3 platform login via Privy (wallet authentication)"

**Description enhanced with:**
- Clear purpose statement
- Step-by-step authentication flow (5 steps)
- Explicit "This is NOT for social verification" section
- References to separate social verification endpoints

**Key additions:**
- Authentication flow visualization
- Clear separation note that social verification uses different endpoints (`/creator/profile/social-verification` and `/social/*`)

---

#### `/auth/verify-signature` (POST)

**Summary updated to:** "Verify wallet signature and issue AW3 platform access tokens"

**Description enhanced with:**
- Clear purpose: Complete wallet-based auth via Privy
- "What this does" section (3 bullets)
- "What this does NOT do" section (3 bullets - explicitly states no social verification)
- "For social verification" section explaining the independent service

**Key clarifications:**
- Does NOT verify social media accounts
- Does NOT fetch social metrics
- Does NOT perform OAuth to social platforms
- Explains where to go for social verification after platform login

---

#### `/auth/register` (POST)

**Summary updated to:** "Register new user on AW3 platform"

**Description massively enhanced with:**
- Clear purpose statement
- Prerequisites section listing Privy auth methods
- Registration flow (3 steps)
- **Critical "Important Distinction" section:**
  - Privy Twitter login vs Social Verification explained
  - Examples of how these are TWO SEPARATE processes
  - Shows a user can log in with one method but verify social accounts with another

**Key additions:**
- Distinction between Privy Twitter login (platform access) and Twitter social verification (metrics)
- Examples showing users can mix and match login methods and verification platforms

---

#### `/auth/refresh` (POST)

**Summary updated to:** "Refresh AW3 platform access token"

**Description added:**
- Clarifies this only refreshes AW3 platform tokens
- Notes social verification tokens managed separately

---

#### `/auth/logout` (POST)

**Summary updated to:** "Logout from AW3 platform"

**Description added:**
- Clarifies logout is from platform only
- Notes social verifications remain valid (don't need re-verification after re-login)

---

#### `/auth/nonce/{walletAddress}` (GET)

**Summary updated to:** "Get nonce for wallet authentication to AW3 platform"

**Description added:**
- Purpose: Platform authentication only, not for social verification

---

### 4. Social Verification Endpoint Updates

#### `/creator/profile/social-verification` (POST)

**Summary updated to:** "Initiate social account verification (independent service)"

**Description completely rewritten with:**
- **CRITICAL ARCHITECTURE NOTE** at the top
- Purpose section (verify ownership and fetch metrics)
- Architecture section (4 bullets):
  - Independent backend service (NOT Privy)
  - Own OAuth 2.0 implementation
  - Direct platform API calls
  - No Privy dependency
- Complete verification flow (8 steps showing entire OAuth process)
- "What Gets Stored" section (5 items)
- "What Does NOT Get Stored" section (3 items - including NO token storage in MVP)
- Example scenario showing Gmail login + Twitter verification
- Related endpoints references

**Key technical details added:**
- OAuth flow steps with platform redirects
- Token exchange and metric fetching process
- Data storage policies (MVP: one-time verification, tokens discarded)
- Privacy and security notes

---

### 5. NEW Social Verification Service Endpoints

Added comprehensive section: **SOCIAL VERIFICATION SERVICE ENDPOINTS**

**Section header notes:**
- These endpoints are part of INDEPENDENT social verification service
- NOT part of Privy authentication
- Completely separate OAuth implementation

---

#### `/social/{platform}/authorize-url` (GET) - NEW

**Purpose:** Get OAuth authorization URL for social platform

**Comprehensive documentation includes:**
- Architecture notes (independent service, PKCE, state management)
- Complete flow (5 steps from frontend call to callback)
- Supported platforms (Twitter, Discord, Telegram, Instagram, YouTube, TikTok)
- Important note: Separate from Privy even if user logged in with Twitter via Privy

**Request:**
- Path parameter: platform (enum with 6 platforms)

**Response:**
- OAuth authorization URL (full Twitter example provided)
- State parameter
- Platform confirmation

---

#### `/social/callback` (GET) - NEW

**Purpose:** Handle OAuth callback from social platform

**Comprehensive documentation includes:**
- Backend processing steps (6 steps)
- Platform API call examples (Twitter API v2 endpoints)
- Data fetched per platform:
  - Twitter: followers, engagement metrics
  - Discord: guilds, members
  - Telegram: subscribers
- Data storage policies
- Security notes (state validation, PKCE, server-side only)

**Request:**
- Query parameters: code, state, error, error_description

**Response:**
- 302 redirect to frontend with success/error status
- URL examples for both success and error cases

---

#### `/social/status` (GET) - NEW

**Purpose:** Get social verification status for all platforms

**Returns:**
- List of verified social accounts
- Current follower counts (with timestamp)
- Verification timestamps
- Data freshness indicators ("X days old")

**Response includes:**
- Array of social accounts with:
  - Platform, handle, verified status
  - Followers count, engagement rate
  - Verification timestamp
  - Days since verification (data freshness)
  - Reverification flag (if > 90 days)

---

#### `/social/{platform}/disconnect` (DELETE) - NEW

**Purpose:** Remove social account verification data

**Documentation includes:**
- What gets deleted (verification record, metrics, associated data)
- GDPR/Privacy compliance note (supports right to deletion)

**Request:**
- Path parameter: platform (enum with 6 platforms)

**Response:**
- 200: Success
- 404: No verification found

---

## Key Improvements

### 1. Clear Architectural Separation
- Every auth endpoint now explicitly states it's for platform access only
- Every social verification endpoint explicitly states it's independent from Privy
- No ambiguity about which system handles what

### 2. Comprehensive Documentation
- Full OAuth flow documentation for social verification
- Step-by-step processes with numbered lists
- "What this does" and "What this does NOT do" sections
- Example scenarios showing real use cases

### 3. Security and Privacy Clarity
- Clear data storage policies
- Token management explained (MVP: one-time, tokens discarded)
- Privacy notes (no PII storage, hashed IDs)
- GDPR compliance mentions

### 4. Developer Experience
- Related endpoint references throughout
- Clear flow diagrams in descriptions
- Example URLs and request/response structures
- Platform-specific technical details (Twitter API v2, Discord API, etc.)

---

## Technical Details from Social_Verification_Analysis.txt

The changes align with the analysis document's recommendations:

### MVP Implementation (Reflected in API)
- **One-time verification**: Tokens discarded after initial fetch (documented in endpoints)
- **90-day expiration**: Manual re-verification required (shown in `/social/status` response)
- **No continuous monitoring**: MVP focuses on initial verification only
- **Independent OAuth**: Service implements own OAuth 2.0 flows (clearly documented)

### Future Scalability (Documented for reference)
- Endpoints designed to support future continuous monitoring
- Data freshness indicators prepare for automated updates
- Architecture notes mention optional continuous monitoring in post-MVP

### Security Best Practices (All documented)
- State parameter CSRF protection
- PKCE code verifier validation
- Server-side only token exchange
- Hashed user IDs for privacy
- No token storage in MVP

---

## Validation

The updated `swagger.yaml` file:
- Maintains valid OpenAPI 3.0.3 syntax
- Preserves all existing endpoints and schemas
- Adds new social verification endpoints
- Enhances documentation without breaking changes
- Provides clear migration path from any Privy-coupled design to independent architecture

---

## References

1. **Social_Verification_Analysis.txt**: Sections 3.1, 3.2, 3.3 on architectural decisions
2. **Privy Documentation**: https://docs.privy.io/authentication/overview
   - Privy handles platform authentication (email/social/wallet)
   - Privy does NOT handle application-specific social verification
3. **OAuth 2.0 Best Practices**: PKCE, state parameters, server-side token exchange

---

## Next Steps

1. **Backend Implementation**:
   - Implement independent social verification service
   - Set up OAuth 2.0 flows for Twitter, Discord, Telegram
   - Implement token exchange and metric fetching
   - Set up secure state/verifier storage (Redis)

2. **Frontend Integration**:
   - Separate Privy login UI from social verification UI
   - Implement OAuth redirect flow for social verification
   - Display data freshness indicators
   - Handle re-verification prompts at 90 days

3. **Testing**:
   - Test complete OAuth flows for each platform
   - Verify data storage and privacy compliance
   - Test error handling (user denial, token expiration)
   - Load test with multiple concurrent verifications

---

## Summary

The API specification now clearly reflects the correct architecture:
- **Privy** = AW3 platform authentication ONLY
- **Social Verification Service** = Independent OAuth implementation for social metrics
- **No coupling** between the two systems
- **Clear documentation** for developers implementing both systems

This separation ensures:
- Scalability (can add new social platforms without affecting Privy)
- Flexibility (users can log in with any method, verify any social accounts)
- Security (independent services with separate security boundaries)
- Compliance (clear data handling policies for each system)
