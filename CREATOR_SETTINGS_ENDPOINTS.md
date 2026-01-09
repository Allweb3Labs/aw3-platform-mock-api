# Creator Settings Endpoints - Complete Documentation

## Overview

The Creator Settings endpoints provide comprehensive management of user preferences across notification, privacy, language, rate, and security settings.

**Important Note:** Wallet and Security settings are managed by Privy, not by the AW3 backend.

---

## 1. Notification Settings

### Endpoints

**GET /creator/settings/notification**
**POST /creator/settings/notification**

### Purpose

Manage email and push notification preferences for various platform events.

### Schema: NotificationSettings

```json
{
  "email": {
    "newCampaignMatches": true,
    "applicationStatusUpdates": false,
    "paymentReceived": true,
    "messagesFromClients": true,
    "communityInteractions": false,
    "platformAnnouncements": false
  },
  "push": {
    "realTimeMessages": true,
    "campaignDeadlines": true,
    "paymentConfirmations": false,
    "milestoneReminders": false
  },
  "frequency": "instant"
}
```

### Email Notification Types

| Field | Description | Default |
|-------|-------------|---------|
| `newCampaignMatches` | Notify when new campaigns match user profile | true |
| `applicationStatusUpdates` | Notify when application status changes | true |
| `paymentReceived` | Notify when payment is received | true |
| `messagesFromClients` | Notify when receiving messages from projects | true |
| `communityInteractions` | Notify about community interactions | false |
| `platformAnnouncements` | Notify about platform updates | false |

### Push Notification Types

| Field | Description | Default |
|-------|-------------|---------|
| `realTimeMessages` | Push notifications for real-time messages | true |
| `campaignDeadlines` | Push notifications for campaign deadlines | true |
| `paymentConfirmations` | Push notifications for payments | false |
| `milestoneReminders` | Push notifications for milestone reminders | false |

### Frequency Options

| Value | Description |
|-------|-------------|
| `instant` | Receive notifications immediately |
| `daily_digest` | Receive once per day summary |
| `weekly_summary` | Receive once per week summary |

### GET Example

```javascript
const response = await fetch('/api/creator/settings/notification', {
  headers: { 'Authorization': `Bearer ${token}` }
});

const { data } = await response.json();
console.log(data.frequency); // "instant"
```

### POST Example

```javascript
const response = await fetch('/api/creator/settings/notification', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: {
      newCampaignMatches: true,
      applicationStatusUpdates: true,
      paymentReceived: true,
      messagesFromClients: true,
      communityInteractions: false,
      platformAnnouncements: false
    },
    push: {
      realTimeMessages: true,
      campaignDeadlines: true,
      paymentConfirmations: false,
      milestoneReminders: false
    },
    frequency: 'daily_digest'
  })
});
```

### UI Implementation (React)

```jsx
import { useState, useEffect } from 'react';

function NotificationSettings() {
  const [settings, setSettings] = useState({
    email: {
      newCampaignMatches: true,
      applicationStatusUpdates: false,
      paymentReceived: true,
      messagesFromClients: true,
      communityInteractions: false,
      platformAnnouncements: false
    },
    push: {
      realTimeMessages: true,
      campaignDeadlines: true,
      paymentConfirmations: false,
      milestoneReminders: false
    },
    frequency: 'instant'
  });

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    const response = await fetch('/api/creator/settings/notification', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const { data } = await response.json();
    setSettings(data);
  };

  const handleEmailToggle = (field) => {
    setSettings(prev => ({
      ...prev,
      email: {
        ...prev.email,
        [field]: !prev.email[field]
      }
    }));
  };

  const handlePushToggle = (field) => {
    setSettings(prev => ({
      ...prev,
      push: {
        ...prev.push,
        [field]: !prev.push[field]
      }
    }));
  };

  const handleFrequencyChange = (frequency) => {
    setSettings(prev => ({ ...prev, frequency }));
  };

  const handleSave = async () => {
    const response = await fetch('/api/creator/settings/notification', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(settings)
    });
    
    if (response.ok) {
      alert('Settings saved successfully');
    }
  };

  return (
    <div>
      <h2>Notification Preferences</h2>
      
      <section>
        <h3>Email Notifications</h3>
        <label>
          <input
            type="checkbox"
            checked={settings.email.newCampaignMatches}
            onChange={() => handleEmailToggle('newCampaignMatches')}
          />
          New campaign matches
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.email.applicationStatusUpdates}
            onChange={() => handleEmailToggle('applicationStatusUpdates')}
          />
          Application status updates
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.email.paymentReceived}
            onChange={() => handleEmailToggle('paymentReceived')}
          />
          Payment received
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.email.messagesFromClients}
            onChange={() => handleEmailToggle('messagesFromClients')}
          />
          Messages from clients
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.email.communityInteractions}
            onChange={() => handleEmailToggle('communityInteractions')}
          />
          Community interactions
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.email.platformAnnouncements}
            onChange={() => handleEmailToggle('platformAnnouncements')}
          />
          Platform announcements
        </label>
      </section>

      <section>
        <h3>Push Notifications</h3>
        <label>
          <input
            type="checkbox"
            checked={settings.push.realTimeMessages}
            onChange={() => handlePushToggle('realTimeMessages')}
          />
          Real-time messages
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.push.campaignDeadlines}
            onChange={() => handlePushToggle('campaignDeadlines')}
          />
          Campaign deadlines
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.push.paymentConfirmations}
            onChange={() => handlePushToggle('paymentConfirmations')}
          />
          Payment confirmations
        </label>
        <label>
          <input
            type="checkbox"
            checked={settings.push.milestoneReminders}
            onChange={() => handlePushToggle('milestoneReminders')}
          />
          Milestone reminders
        </label>
      </section>

      <section>
        <h3>Notification Frequency</h3>
        <label>
          <input
            type="radio"
            checked={settings.frequency === 'instant'}
            onChange={() => handleFrequencyChange('instant')}
          />
          Instant
        </label>
        <label>
          <input
            type="radio"
            checked={settings.frequency === 'daily_digest'}
            onChange={() => handleFrequencyChange('daily_digest')}
          />
          Daily digest
        </label>
        <label>
          <input
            type="radio"
            checked={settings.frequency === 'weekly_summary'}
            onChange={() => handleFrequencyChange('weekly_summary')}
          />
          Weekly summary
        </label>
      </section>

      <button onClick={handleSave}>Save Settings</button>
    </div>
  );
}
```

---

## 2. Privacy Settings

### Endpoints

**GET /creator/settings/privacy**
**POST /creator/settings/privacy**

### Purpose

Manage profile visibility and information sharing preferences.

### Schema: PrivacySettings

```json
{
  "profileVisibility": "PUBLIC",
  "showEarnings": false,
  "showCompletedCampaigns": true,
  "showSocialAccounts": true
}
```

### Fields

| Field | Type | Description | Options/Default |
|-------|------|-------------|-----------------|
| `profileVisibility` | string (required) | Profile visibility level | PUBLIC, PRIVATE, CONNECTIONS_ONLY |
| `showEarnings` | boolean | Display earnings on profile | Default: false |
| `showCompletedCampaigns` | boolean | Display campaign count | Default: true |
| `showSocialAccounts` | boolean | Display social accounts | Default: true |

### Profile Visibility Options

| Value | Description |
|-------|-------------|
| `PUBLIC` | Profile visible to everyone |
| `PRIVATE` | Profile only visible to you |
| `CONNECTIONS_ONLY` | Profile visible only to connected users |

### Example Usage

```javascript
// Get privacy settings
const response = await fetch('/api/creator/settings/privacy', {
  headers: { 'Authorization': `Bearer ${token}` }
});

// Update privacy settings
await fetch('/api/creator/settings/privacy', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    profileVisibility: 'CONNECTIONS_ONLY',
    showEarnings: false,
    showCompletedCampaigns: true,
    showSocialAccounts: true
  })
});
```

---

## 3. Security & Wallet Settings (Privy-Managed)

### Important Notice

**Security and wallet settings are NOT managed by the AW3 backend.**

All security features are handled by Privy:
- Two-factor authentication (2FA)
- Login alerts
- Trusted devices
- Wallet management
- Password reset
- Account recovery

### Endpoints (Deprecated)

**GET /creator/settings/security** (deprecated)
**POST /creator/settings/security** (deprecated)

These endpoints are deprecated and return a message directing users to Privy.

### Privy Resources

- **Privy Dashboard**: https://dashboard.privy.io
- **Security Documentation**: https://docs.privy.io/security/overview
- **User Management**: https://docs.privy.io/user-management/overview

### Implementation

For security settings in your UI, integrate Privy's SDK:

```javascript
import { usePrivy } from '@privy-io/react-auth';

function SecuritySettings() {
  const { user, linkEmail, linkWallet } = usePrivy();
  
  // Use Privy's built-in methods for security management
  return (
    <div>
      <h2>Security Settings</h2>
      <p>Security settings are managed by Privy.</p>
      <button onClick={linkEmail}>Add Email</button>
      <button onClick={linkWallet}>Link Wallet</button>
      <a href="https://dashboard.privy.io" target="_blank">
        Manage Security Settings
      </a>
    </div>
  );
}
```

---

## 4. Other Settings

### Language Settings

**GET /creator/settings/language**
**POST /creator/settings/language**

See: `LOCALIZATION_AND_RATE_SETTINGS.md`

### Rate Configuration

**GET /creator/settings/rate**
**POST /creator/settings/rate**

See: `LOCALIZATION_AND_RATE_SETTINGS.md`

---

## Backend Implementation Guide

### Notification Settings Storage

```sql
CREATE TABLE creator_notification_settings (
  creator_id UUID PRIMARY KEY REFERENCES users(id),
  
  -- Email notifications
  email_new_campaign_matches BOOLEAN DEFAULT TRUE,
  email_application_status_updates BOOLEAN DEFAULT TRUE,
  email_payment_received BOOLEAN DEFAULT TRUE,
  email_messages_from_clients BOOLEAN DEFAULT TRUE,
  email_community_interactions BOOLEAN DEFAULT FALSE,
  email_platform_announcements BOOLEAN DEFAULT FALSE,
  
  -- Push notifications
  push_real_time_messages BOOLEAN DEFAULT TRUE,
  push_campaign_deadlines BOOLEAN DEFAULT TRUE,
  push_payment_confirmations BOOLEAN DEFAULT FALSE,
  push_milestone_reminders BOOLEAN DEFAULT FALSE,
  
  -- Frequency
  notification_frequency VARCHAR(20) DEFAULT 'instant' CHECK (notification_frequency IN ('instant', 'daily_digest', 'weekly_summary')),
  
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Privacy Settings Storage

```sql
CREATE TABLE creator_privacy_settings (
  creator_id UUID PRIMARY KEY REFERENCES users(id),
  profile_visibility VARCHAR(20) DEFAULT 'PUBLIC' CHECK (profile_visibility IN ('PUBLIC', 'PRIVATE', 'CONNECTIONS_ONLY')),
  show_earnings BOOLEAN DEFAULT FALSE,
  show_completed_campaigns BOOLEAN DEFAULT TRUE,
  show_social_accounts BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Validation Example

```javascript
const validateNotificationSettings = (settings) => {
  // Validate email object
  if (!settings.email || typeof settings.email !== 'object') {
    throw new Error('Email settings are required');
  }
  
  const emailFields = [
    'newCampaignMatches',
    'applicationStatusUpdates',
    'paymentReceived',
    'messagesFromClients',
    'communityInteractions',
    'platformAnnouncements'
  ];
  
  for (const field of emailFields) {
    if (typeof settings.email[field] !== 'boolean') {
      throw new Error(`Email field '${field}' must be boolean`);
    }
  }
  
  // Validate push object
  if (!settings.push || typeof settings.push !== 'object') {
    throw new Error('Push settings are required');
  }
  
  const pushFields = [
    'realTimeMessages',
    'campaignDeadlines',
    'paymentConfirmations',
    'milestoneReminders'
  ];
  
  for (const field of pushFields) {
    if (typeof settings.push[field] !== 'boolean') {
      throw new Error(`Push field '${field}' must be boolean`);
    }
  }
  
  // Validate frequency
  const validFrequencies = ['instant', 'daily_digest', 'weekly_summary'];
  if (!validFrequencies.includes(settings.frequency)) {
    throw new Error('Invalid frequency value');
  }
  
  return true;
};
```

---

## Testing Checklist

### Notification Settings
- [ ] GET returns all notification settings
- [ ] POST updates all notification settings
- [ ] All boolean fields accept true/false
- [ ] Frequency enum validates correctly
- [ ] Invalid frequency values are rejected
- [ ] Missing required fields return 400 error
- [ ] Settings persist after update

### Privacy Settings
- [ ] GET returns current privacy settings
- [ ] POST updates privacy settings
- [ ] profileVisibility enum validates correctly
- [ ] Invalid visibility values are rejected
- [ ] Boolean fields work correctly
- [ ] Required field validation works

### Security Settings (Privy)
- [ ] GET returns deprecation message
- [ ] POST returns deprecation message
- [ ] Documentation directs to Privy
- [ ] Frontend integrates Privy SDK for security

---

## Summary

**Fully Implemented:**
- Notification Settings (email, push, frequency)
- Privacy Settings (visibility, information sharing)
- Language Settings (see LOCALIZATION_AND_RATE_SETTINGS.md)
- Rate Configuration (see LOCALIZATION_AND_RATE_SETTINGS.md)

**Managed by Privy:**
- Security Settings (2FA, login alerts, trusted devices)
- Wallet Settings (wallet management, connections)

All endpoints include comprehensive documentation, examples, and validation. The notification settings match the UI design exactly.
