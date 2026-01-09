# Developer Quick Reference - AW3 Authentication & Social Verification

## TL;DR

**Two completely separate systems:**
1. **Privy** = Login to AW3 platform
2. **Social Verification Service** = Verify social accounts for metrics

**They are NOT connected. They are NOT dependent. They use different OAuth flows.**

---

## Quick Decision Tree

### "Should I use Privy or Social Verification?"

```
Question: What am I trying to do?

├─ User needs to LOG INTO AW3 platform
│  └─ Use: Privy Authentication
│     └─ Endpoints: /auth/*
│
└─ User needs to VERIFY SOCIAL ACCOUNT for metrics
   └─ Use: Social Verification Service
      └─ Endpoints: /social/*, /creator/profile/social-verification
```

---

## Common Scenarios

### Scenario 1: New User Signs Up
```javascript
// Step 1: Privy authentication (platform login)
import { usePrivy } from '@privy-io/react-auth';

const { login } = usePrivy();

// User clicks "Sign Up with Gmail"
await login();

// Result: User is logged into AW3 platform
// Can now access protected APIs with JWT token
```

### Scenario 2: Creator Verifies Twitter
```javascript
// Step 2: Social verification (fetch metrics)
// NOTE: User MUST be logged in to AW3 first (via Privy)

// Get OAuth URL from social verification service
const response = await fetch('/api/social/twitter/authorize-url', {
  headers: { 'Authorization': `Bearer ${aw3Token}` } // Token from Privy login
});

const { authUrl } = await response.json();

// Redirect user to Twitter
window.location.href = authUrl;

// Twitter redirects back to /social/callback
// Backend fetches metrics and stores verification
```

### Scenario 3: User Logged In with Twitter, Still Needs Verification
```javascript
// Common misconception: "User logged in with Twitter, so Twitter is verified"
// WRONG! These are separate flows.

// User logs in with Twitter (via Privy)
await login(); // Privy handles Twitter OAuth for PLATFORM ACCESS

// User STILL needs to verify Twitter for SOCIAL METRICS
const response = await fetch('/api/social/twitter/authorize-url');
// ... separate OAuth flow to Twitter API for metrics
```

---

## API Endpoints Cheat Sheet

### Platform Authentication (Privy)

| Endpoint | Method | Purpose | When to Use |
|----------|--------|---------|-------------|
| `/auth/wallet-connect` | POST | Start wallet login | User clicks "Connect Wallet" |
| `/auth/verify-signature` | POST | Complete wallet login | After user signs nonce |
| `/auth/register` | POST | Register new user | After Privy authentication |
| `/auth/refresh` | POST | Refresh access token | Token expired |
| `/auth/logout` | POST | Logout from platform | User clicks "Logout" |
| `/auth/nonce/{address}` | GET | Get nonce for wallet | During wallet connection |

### Social Verification (Independent)

| Endpoint | Method | Purpose | When to Use |
|----------|--------|---------|-------------|
| `/creator/profile/social-verification` | POST | Initiate verification | Simplified verification |
| `/social/{platform}/authorize-url` | GET | Get OAuth URL | User clicks "Verify Twitter" |
| `/social/callback` | GET | Handle OAuth callback | Twitter redirects back |
| `/social/status` | GET | Get verification status | Display verified accounts |
| `/social/{platform}/disconnect` | DELETE | Remove verification | User wants to disconnect |

---

## Code Examples

### Frontend: Complete Auth Flow

```typescript
// 1. Platform Authentication (Privy)
import { usePrivy } from '@privy-io/react-auth';

function LoginButton() {
  const { login, authenticated, user } = usePrivy();
  
  const handleLogin = async () => {
    // Privy handles Gmail, Twitter (via Privy), Wallet login
    await login();
    // User is now logged into AW3 platform
    // JWT token managed by Privy SDK
  };
  
  if (authenticated) {
    return <div>Logged in as {user.id}</div>;
  }
  
  return <button onClick={handleLogin}>Login to AW3</button>;
}

// 2. Social Verification (Independent Service)
function SocialVerificationButton({ platform }) {
  const { getAccessToken } = usePrivy(); // Get AW3 platform token
  
  const handleVerify = async () => {
    // User MUST be logged in to AW3 first
    const aw3Token = await getAccessToken();
    
    // Get OAuth URL from social verification service
    const response = await fetch(`/api/social/${platform}/authorize-url`, {
      headers: { 'Authorization': `Bearer ${aw3Token}` }
    });
    
    const { authUrl } = await response.json();
    
    // Redirect to Twitter/Discord/etc for authorization
    window.location.href = authUrl;
    
    // Platform redirects to /social/callback
    // Backend handles OAuth, fetches metrics, stores verification
    // User redirected back to /creator/profile?verification=success
  };
  
  return (
    <button onClick={handleVerify}>
      Verify {platform}
    </button>
  );
}

// 3. Display Verified Accounts
function VerifiedAccounts() {
  const { getAccessToken } = usePrivy();
  const [accounts, setAccounts] = useState([]);
  
  useEffect(() => {
    const fetchAccounts = async () => {
      const token = await getAccessToken();
      const response = await fetch('/api/social/status', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const { data } = await response.json();
      setAccounts(data.socialAccounts);
    };
    
    fetchAccounts();
  }, []);
  
  return (
    <div>
      {accounts.map(account => (
        <div key={account.platform}>
          <span>{account.platform}: {account.handle}</span>
          <span>{account.followersCount} followers</span>
          <span>Verified {account.daysOld} days ago</span>
        </div>
      ))}
    </div>
  );
}
```

### Backend: Social Verification Service

```typescript
// 1. Generate OAuth URL
app.get('/api/social/:platform/authorize-url', authenticate, async (req, res) => {
  const { platform } = req.params;
  const userId = req.user.id; // From Privy JWT token
  
  // Generate secure state and PKCE verifier
  const state = crypto.randomBytes(32).toString('hex');
  const codeVerifier = crypto.randomBytes(32).toString('base64url');
  const codeChallenge = crypto.createHash('sha256')
    .update(codeVerifier)
    .digest('base64url');
  
  // Store in Redis with 10 minute expiry
  await redis.setex(`oauth:${state}`, 600, JSON.stringify({
    userId,
    platform,
    codeVerifier
  }));
  
  // Generate platform-specific OAuth URL
  let authUrl;
  if (platform === 'twitter') {
    authUrl = `https://twitter.com/i/oauth2/authorize?` +
      `response_type=code` +
      `&client_id=${TWITTER_CLIENT_ID}` +
      `&redirect_uri=${encodeURIComponent(CALLBACK_URL)}` +
      `&scope=tweet.read%20users.read%20offline.access` +
      `&state=${state}` +
      `&code_challenge=${codeChallenge}` +
      `&code_challenge_method=S256`;
  }
  // ... similar for discord, telegram, etc.
  
  res.json({ success: true, data: { authUrl, state, platform } });
});

// 2. Handle OAuth Callback
app.get('/api/social/callback', async (req, res) => {
  const { code, state, error } = req.query;
  
  // Handle user denial
  if (error) {
    return res.redirect(`/creator/profile?error=${error}`);
  }
  
  // Validate state (CSRF protection)
  const session = await redis.get(`oauth:${state}`);
  if (!session) {
    return res.status(400).json({ error: 'Invalid or expired state' });
  }
  
  const { userId, platform, codeVerifier } = JSON.parse(session);
  
  // Exchange code for token (example: Twitter)
  const tokenResponse = await fetch('https://api.twitter.com/2/oauth2/token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'Authorization': `Basic ${Buffer.from(`${TWITTER_CLIENT_ID}:${TWITTER_CLIENT_SECRET}`).toString('base64')}`
    },
    body: new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      redirect_uri: CALLBACK_URL,
      code_verifier: codeVerifier
    })
  });
  
  const { access_token } = await tokenResponse.json();
  
  // Fetch user profile and metrics
  const profileResponse = await fetch(
    'https://api.twitter.com/2/users/me?user.fields=public_metrics',
    {
      headers: { 'Authorization': `Bearer ${access_token}` }
    }
  );
  
  const { data: profile } = await profileResponse.json();
  
  // Store verification (NO token storage in MVP)
  await db.query(`
    INSERT INTO creator_social_accounts 
    (creator_id, platform, platform_user_id_hash, handle, followers_count, verified_at)
    VALUES ($1, $2, $3, $4, $5, NOW())
    ON CONFLICT (creator_id, platform) DO UPDATE SET
      followers_count = $5,
      verified_at = NOW()
  `, [
    userId,
    platform,
    crypto.createHash('sha256').update(`${platform}:${profile.id}:${SALT}`).digest('hex'),
    `@${profile.username}`,
    profile.public_metrics.followers_count
  ]);
  
  // Token discarded (not stored) in MVP
  
  // Redirect to success page
  res.redirect(`/creator/profile?verification=success&platform=${platform}`);
});

// 3. Get Verification Status
app.get('/api/social/status', authenticate, async (req, res) => {
  const userId = req.user.id;
  
  const accounts = await db.query(`
    SELECT platform, handle, followers_count, engagement_rate, verified_at,
           EXTRACT(DAY FROM NOW() - verified_at) as days_old,
           EXTRACT(DAY FROM NOW() - verified_at) > 90 as requires_reverification
    FROM creator_social_accounts
    WHERE creator_id = $1
  `, [userId]);
  
  res.json({
    success: true,
    data: {
      socialAccounts: accounts.rows.map(row => ({
        platform: row.platform,
        handle: row.handle,
        verified: true,
        followersCount: row.followers_count,
        engagementRate: row.engagement_rate,
        verifiedAt: row.verified_at,
        daysOld: row.days_old,
        requiresReverification: row.requires_reverification
      }))
    }
  });
});
```

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Thinking Privy handles social verification
```javascript
// WRONG
const { user } = usePrivy();
// Assuming user.linkedAccounts gives social metrics
// NO! Privy only handles platform authentication
```

```javascript
// CORRECT
const { user } = usePrivy(); // Platform authentication
const socialAccounts = await fetch('/api/social/status'); // Social verification
```

---

### ❌ Mistake 2: Confusing Twitter login with Twitter verification
```javascript
// WRONG
await privyLogin({ provider: 'twitter' }); // User logged in with Twitter
// Assuming Twitter is now verified for metrics
// NO! These are separate OAuth flows
```

```javascript
// CORRECT
await privyLogin({ provider: 'twitter' }); // Login to AW3 platform
// User STILL needs to verify Twitter separately:
await fetch('/api/social/twitter/authorize-url'); // Verify for metrics
```

---

### ❌ Mistake 3: Storing social OAuth tokens with Privy data
```javascript
// WRONG
// Trying to store Twitter OAuth tokens in Privy user object
// NO! Social verification is independent service
```

```javascript
// CORRECT
// Privy manages platform authentication tokens (hidden from you)
// Social verification service manages social OAuth separately
// In MVP: social tokens discarded after fetching metrics
```

---

### ❌ Mistake 4: Assuming user can only verify the login platform
```javascript
// WRONG
if (user.loginMethod === 'gmail') {
  // Only allowing Gmail-related verifications
  // NO! User can verify any social platform regardless of login method
}
```

```javascript
// CORRECT
// User logged in with Gmail can verify Twitter, Discord, Telegram, etc.
// User logged in with Wallet can verify any social platform
// Login method and social verifications are independent
```

---

## Testing Checklist

### Platform Authentication (Privy)
- [ ] User can log in with Gmail
- [ ] User can log in with Twitter (via Privy)
- [ ] User can log in with Wallet (MetaMask)
- [ ] JWT token issued after login
- [ ] Token refresh works
- [ ] Logout invalidates token
- [ ] Can access protected endpoints with token

### Social Verification (Independent)
- [ ] User must be logged in to AW3 first (authenticated via Privy)
- [ ] Can get OAuth URL for Twitter
- [ ] Can get OAuth URL for Discord
- [ ] Can get OAuth URL for Telegram
- [ ] Twitter OAuth flow completes successfully
- [ ] Follower count fetched from Twitter API
- [ ] Engagement rate calculated correctly
- [ ] Verification data stored in database
- [ ] OAuth tokens NOT stored (MVP)
- [ ] Can view verification status
- [ ] Can disconnect social account
- [ ] User who logged in with Twitter can STILL verify Twitter separately
- [ ] User who logged in with Gmail can verify Twitter, Discord, etc.

### Integration Testing
- [ ] Login with Gmail → Verify Twitter → Success
- [ ] Login with Twitter (Privy) → Verify Twitter (social) → Success
- [ ] Login with Wallet → Verify multiple platforms → Success
- [ ] Logout → Social verifications remain → Success
- [ ] Re-login → Can see previous verifications → Success

---

## FAQ

### Q: Can a user log in with Twitter and verify Twitter?
**A:** Yes! These are two separate processes:
- **Privy Twitter login**: Proves user owns Twitter account to access AW3 platform
- **Social verification**: Fetches Twitter metrics (followers, engagement) for campaigns

Even if user logged in with Twitter via Privy, they still need to go through social verification flow separately.

---

### Q: Why are these separate? Can't Privy handle both?
**A:** Privy is designed for authentication (identity verification), not for fetching application-specific metrics. Privy:
- Doesn't provide APIs to fetch follower counts, engagement rates
- Doesn't support continuous monitoring of metrics
- Is optimized for authentication, not metric tracking

Social verification needs:
- Direct access to platform APIs (Twitter API, Discord API)
- Own OAuth implementation with specific scopes
- Metric calculation logic
- Data storage for campaign matching

---

### Q: What happens to social verifications when user logs out?
**A:** Social verifications remain valid. They are stored in the database and associated with the user account, not the session. When user logs back in (via any method), their verified social accounts are still there.

---

### Q: Can user verify social accounts before logging in?
**A:** No. User must be authenticated to AW3 platform first (via Privy). Social verification endpoints require AW3 JWT token in the Authorization header.

---

### Q: Where are OAuth tokens stored?
**A:** 
- **Privy tokens**: Managed by Privy service (hidden from your app)
- **Social verification tokens**: Discarded after fetching metrics in MVP (not stored)

---

### Q: Can a user have multiple verified social accounts?
**A:** Yes! A user can verify Twitter, Discord, Telegram, Instagram, YouTube, TikTok - all for the same AW3 account. Each verification is independent.

---

## References

- **Swagger API**: `swagger.yaml` - Complete API specification
- **Architecture Diagram**: `ARCHITECTURE_DIAGRAM.md` - Visual architecture
- **Changes Summary**: `CHANGES_SUMMARY.md` - Detailed changelog
- **Analysis Document**: `Social_Verification_Analysis.txt` - Technical analysis
- **Privy Docs**: https://docs.privy.io/authentication/overview

---

## Quick Command Reference

```bash
# Read current OAuth status
GET /api/social/status

# Initiate Twitter verification
GET /api/social/twitter/authorize-url

# Handle callback (automatic)
GET /api/social/callback?code={code}&state={state}

# Disconnect Twitter
DELETE /api/social/twitter/disconnect

# Login to AW3 (handled by Privy SDK)
# (No direct API call - use Privy React SDK)
```

---

## Summary

```
┌─────────────────────────────────────────────────────────┐
│  Remember:                                              │
│                                                         │
│  Privy = LOGIN to AW3                                   │
│  Social Verification = FETCH METRICS from platforms     │
│                                                         │
│  They are INDEPENDENT.                                  │
│  They are NOT CONNECTED.                                │
│  They use DIFFERENT OAuth flows.                        │
│                                                         │
│  A user needs BOTH:                                     │
│  1. Login to AW3 (via Privy)                            │
│  2. Verify social accounts (via social service)         │
└─────────────────────────────────────────────────────────┘
```
