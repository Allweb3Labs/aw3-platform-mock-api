# Match Rate Calculation - Frontend Responsibility

## Decision

**Match Rate is NOT included in the GET /creator/campaigns API response.**

The server does not and should not calculate match rates for campaigns because this calculation requires context-specific comparison between:
1. The current user's profile data
2. Each campaign's specific criteria

## Rationale

### Why Match Rate Cannot Be Calculated Server-Side

1. **User Context Required**
   - Match rate is relative to the logged-in user
   - Server would need to recalculate for every user on every request
   - Inefficient and computationally expensive

2. **Dynamic Criteria**
   - Campaign criteria may include multiple factors:
     - Minimum reputation score
     - Minimum follower count
     - Verified social accounts
     - Platform experience
     - Focus area alignment
   - Each factor may have different weights
   - Weights may vary by campaign type

3. **Real-Time Calculation**
   - User profile data may change frequently
   - Match rate needs to reflect current state
   - Frontend already has user profile data in context

4. **Performance**
   - Calculating match rate for every campaign in the list would add significant server load
   - Frontend can calculate asynchronously without blocking API response
   - Client-side calculation scales better with user base

## Frontend Implementation Guide

### Data Required

The frontend needs two pieces of data to calculate match rate:

#### 1. User Profile Data (from /creator/profile/me)
```json
{
  "reputation": 850,
  "socialAccounts": [
    {
      "platform": "twitter",
      "verified": true,
      "followersCount": 125000,
      "engagementRate": 8.5
    }
  ],
  "cvpiScore": 42.5,
  "focusArea": [2, 3],
  "completedCampaigns": 15
}
```

#### 2. Campaign Criteria (from campaign data)
```json
{
  "campaignId": "uuid",
  "requiredReputation": 500,
  "focusArea": 2,
  "kpiTargets": {
    "minimumFollowers": 50000,
    "platforms": ["twitter"]
  }
}
```

### Calculation Logic Example

```javascript
/**
 * Calculate match rate between user profile and campaign criteria
 * @param {Object} userProfile - User's profile data
 * @param {Object} campaign - Campaign data with criteria
 * @returns {number} Match rate percentage (0-100)
 */
function calculateMatchRate(userProfile, campaign) {
  const factors = [];
  
  // Factor 1: Reputation requirement (30% weight)
  if (campaign.requiredReputation) {
    const reputationMatch = Math.min(
      100,
      (userProfile.reputation / campaign.requiredReputation) * 100
    );
    factors.push({ weight: 0.3, score: reputationMatch });
  }
  
  // Factor 2: Focus area alignment (20% weight)
  if (campaign.focusArea) {
    const focusAreaMatch = userProfile.focusArea.includes(campaign.focusArea) 
      ? 100 
      : 0;
    factors.push({ weight: 0.2, score: focusAreaMatch });
  }
  
  // Factor 3: Follower count (30% weight)
  if (campaign.kpiTargets?.minimumFollowers) {
    const userFollowers = getUserTotalFollowers(userProfile.socialAccounts);
    const followersMatch = Math.min(
      100,
      (userFollowers / campaign.kpiTargets.minimumFollowers) * 100
    );
    factors.push({ weight: 0.3, score: followersMatch });
  }
  
  // Factor 4: Platform verification (20% weight)
  if (campaign.kpiTargets?.platforms) {
    const verifiedPlatforms = userProfile.socialAccounts
      .filter(acc => acc.verified)
      .map(acc => acc.platform);
    
    const requiredVerified = campaign.kpiTargets.platforms
      .filter(p => verifiedPlatforms.includes(p));
    
    const platformMatch = campaign.kpiTargets.platforms.length > 0
      ? (requiredVerified.length / campaign.kpiTargets.platforms.length) * 100
      : 100;
    
    factors.push({ weight: 0.2, score: platformMatch });
  }
  
  // Calculate weighted average
  const totalWeight = factors.reduce((sum, f) => sum + f.weight, 0);
  const weightedScore = factors.reduce((sum, f) => sum + (f.score * f.weight), 0);
  
  return totalWeight > 0 ? Math.round(weightedScore / totalWeight) : 0;
}

/**
 * Helper: Get total followers across all verified accounts
 */
function getUserTotalFollowers(socialAccounts) {
  return socialAccounts
    .filter(acc => acc.verified)
    .reduce((sum, acc) => sum + acc.followersCount, 0);
}
```

### React Implementation Example

```jsx
import { useMemo } from 'react';

function CampaignList({ campaigns, userProfile }) {
  // Calculate match rates for all campaigns
  const campaignsWithMatchRate = useMemo(() => {
    return campaigns.map(campaign => ({
      ...campaign,
      matchRate: calculateMatchRate(userProfile, campaign)
    }));
  }, [campaigns, userProfile]);
  
  // Sort by match rate (optional)
  const sortedCampaigns = useMemo(() => {
    return [...campaignsWithMatchRate].sort((a, b) => 
      b.matchRate - a.matchRate
    );
  }, [campaignsWithMatchRate]);
  
  return (
    <div>
      {sortedCampaigns.map(campaign => (
        <CampaignCard 
          key={campaign.campaignId}
          campaign={campaign}
          matchRate={campaign.matchRate}
        />
      ))}
    </div>
  );
}

function CampaignCard({ campaign, matchRate }) {
  // Color code match rate
  const matchRateColor = 
    matchRate >= 80 ? 'green' :
    matchRate >= 60 ? 'yellow' :
    matchRate >= 40 ? 'orange' : 'red';
  
  return (
    <div className="campaign-card">
      <h3>{campaign.title}</h3>
      <div className={`match-rate ${matchRateColor}`}>
        {matchRate}% Match
      </div>
      {/* Rest of campaign details */}
    </div>
  );
}
```

## API Response Structure

### Current Response (Correct)

```json
{
  "success": true,
  "data": {
    "campaigns": [
      {
        "campaignId": "uuid",
        "projectId": "uuid",
        "title": "NFT Marketing Campaign",
        "status": 3,
        "budgetAmount": 5000,
        "budgetToken": 2,
        "numberOfApplicants": 12,
        "numberOfDeliveries": 3,
        "deadline": "2025-12-31T23:59:59Z",
        "kpiTargets": {
          "minimumFollowers": 50000,
          "platforms": ["twitter"]
        },
        "requiredReputation": 500,
        "projectAvatar": "https://...",
        "createdAt": "2025-12-01T00:00:00Z",
        "updatedAt": "2025-12-15T12:00:00Z"
      }
    ],
    "pagination": {
      "page": 0,
      "size": 20,
      "total": 100
    }
  }
}
```

**Note:** No matchRate field in the response.

### Frontend Processing

```javascript
// 1. Fetch campaigns
const response = await fetch('/api/creator/campaigns');
const { data } = await response.json();

// 2. Get user profile (if not already in context)
const profileResponse = await fetch('/api/creator/profile/me');
const { data: userProfile } = await profileResponse.json();

// 3. Calculate match rates on frontend
const campaignsWithMatchRate = data.campaigns.map(campaign => ({
  ...campaign,
  matchRate: calculateMatchRate(userProfile, campaign)
}));

// 4. Display with match rates
displayCampaigns(campaignsWithMatchRate);
```

## Match Rate Display Guidelines

### Visual Indicators

```
90-100%: Excellent Match (Green)
  - All or nearly all criteria met
  - Highly recommended
  
70-89%: Good Match (Light Green)
  - Most criteria met
  - Worth applying
  
50-69%: Moderate Match (Yellow)
  - Some criteria met
  - Consider applying if interested
  
30-49%: Low Match (Orange)
  - Few criteria met
  - May be challenging
  
0-29%: Poor Match (Red)
  - Criteria mostly not met
  - Not recommended
```

### UI Examples

```
Example 1: High Match
┌─────────────────────────────────────┐
│ NFT Marketing Campaign              │
│ [95% Match ✓]                       │
│ Budget: $5,000 USDC                 │
│ Deadline: Dec 31, 2025              │
│                                     │
│ You exceed requirements:            │
│ ✓ Reputation: 850 (need 500)       │
│ ✓ Followers: 125K (need 50K)       │
│ ✓ Focus: NFT ✓                     │
└─────────────────────────────────────┘

Example 2: Moderate Match
┌─────────────────────────────────────┐
│ DeFi Protocol Launch                │
│ [65% Match ~]                       │
│ Budget: $10,000 USDC                │
│ Deadline: Jan 15, 2026              │
│                                     │
│ Requirements status:                │
│ ✓ Reputation: 850 (need 700)       │
│ ~ Followers: 75K (need 100K)        │
│ ✗ Focus: NFT (need DeFi)           │
└─────────────────────────────────────┘
```

## Benefits of Frontend Calculation

1. **Performance**
   - No server-side computation overhead
   - Scales with client devices, not server load
   - Asynchronous calculation doesn't block UI

2. **Flexibility**
   - Easy to adjust calculation logic
   - Can add custom user preferences
   - No API changes needed for algorithm updates

3. **Privacy**
   - Server doesn't need to track user-campaign matching
   - User preferences stay client-side
   - No additional data collection required

4. **Real-Time**
   - Updates immediately when user profile changes
   - No cache invalidation issues
   - Always reflects current state

## Related Endpoints

- **GET /creator/profile/me** - Provides user profile data for calculation
- **GET /creator/campaigns** - Provides campaign list with criteria
- **GET /creator/campaigns/{id}** - Provides detailed campaign criteria

## Summary

Match rate calculation is a client-side responsibility because:
- It requires user-specific context
- It's computationally expensive to calculate server-side for all users
- Frontend has all necessary data available
- Calculation logic can be updated without API changes
- Better performance and scalability

The API provides all necessary data (user profile and campaign criteria) for the frontend to calculate accurate match rates.
