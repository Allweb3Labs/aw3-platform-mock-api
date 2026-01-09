# Localization and Rate Settings Implementation

## Summary

Both requested features are already implemented in the API specification:

1. GET /creator/system/localization endpoint - Returns language and timezone literals
2. Rate configuration with "selected" field and enum values

---

## 1. Localization Endpoint

### Endpoint Details

**GET /creator/system/localization**

- Tags: Creator System
- Summary: Get localization literals (languages and timezones)
- Operation ID: getCreatorLocalizationLiterals
- Security: No authentication required (public endpoint)

### Response Structure

```json
{
  "success": true,
  "data": {
    "languages": [
      { "code": "en", "label": "English" },
      { "code": "en-US", "label": "English (United States)" },
      { "code": "zh-CN", "label": "Chinese (Simplified, China)" },
      { "code": "zh-TW", "label": "Chinese (Traditional, Taiwan)" },
      { "code": "ja-JP", "label": "Japanese (Japan)" },
      { "code": "ko-KR", "label": "Korean (South Korea)" }
    ],
    "timezones": [
      "America/New_York",
      "Europe/London",
      "Asia/Shanghai",
      "Asia/Tokyo",
      "Asia/Seoul"
    ]
  }
}
```

### Schema Definition

**Languages Array:**
- Each language object contains:
  - `code` (string, required): Language code (e.g., "en", "zh-CN")
  - `label` (string, required): Display label (e.g., "English", "Chinese (Simplified, China)")

**Timezones Array:**
- Array of timezone strings in IANA Time Zone Database format
- Examples: "America/New_York", "Asia/Shanghai"

### Usage Example

```javascript
// Fetch localization literals
const response = await fetch('/api/creator/system/localization');
const { data } = await response.json();

// Populate language dropdown
const languageOptions = data.languages.map(lang => ({
  value: lang.code,
  label: lang.label
}));

// Populate timezone dropdown
const timezoneOptions = data.timezones.map(tz => ({
  value: tz,
  label: tz
}));
```

---

## 2. Rate Configuration Endpoint

### Endpoint Details

**GET /creator/settings/rate**

- Tags: Creator Settings
- Summary: Get rate configuration
- Operation ID: getRateConfiguration
- Security: Bearer Auth required

**POST /creator/settings/rate**

- Tags: Creator Settings
- Summary: Update rate configuration
- Operation ID: updateRateConfiguration
- Security: Bearer Auth required

### Response Structure (GET)

```json
{
  "success": true,
  "data": {
    "hourlyRate": 0,
    "dailyRate": 0,
    "projectRate": 0,
    "currency": "USDC",
    "minimumBudget": 0,
    "selected": "hourly"
  }
}
```

### Request Structure (POST)

```json
{
  "hourlyRate": 50,
  "dailyRate": 400,
  "projectRate": 5000,
  "currency": "USDC",
  "minimumBudget": 500,
  "selected": "hourly"
}
```

### Schema Definition (RateConfiguration)

**Required Fields:**
- `hourlyRate` (number): Default hourly rate
- `currency` (string): Currency code (e.g., "USDC")
- `selected` (string): Selected rate mode

**Optional Fields:**
- `dailyRate` (number): Daily rate
- `projectRate` (number): Project-based rate
- `minimumBudget` (number): Minimum budget requirement

**Selected Field Enum:**
- `"hourly"`: Hourly rate selected
- `"daily"`: Daily rate selected
- `"project"`: Project rate selected

### Usage Example

```javascript
// Get current rate configuration
const getRate = async () => {
  const response = await fetch('/api/creator/settings/rate', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const { data } = await response.json();
  console.log(data.selected); // "hourly", "daily", or "project"
};

// Update rate configuration
const updateRate = async () => {
  const response = await fetch('/api/creator/settings/rate', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      hourlyRate: 75,
      dailyRate: 600,
      projectRate: 8000,
      currency: 'USDC',
      minimumBudget: 1000,
      selected: 'daily'  // Switch to daily rate
    })
  });
  const result = await response.json();
};
```

---

## Implementation Guide

### Frontend Implementation

#### 1. Language Settings UI

```jsx
import { useState, useEffect } from 'react';

function LanguageSettings() {
  const [languages, setLanguages] = useState([]);
  const [timezones, setTimezones] = useState([]);
  const [selectedLanguage, setSelectedLanguage] = useState('en');
  const [selectedTimezone, setSelectedTimezone] = useState('UTC');

  // Fetch localization literals on mount
  useEffect(() => {
    const fetchLiterals = async () => {
      const response = await fetch('/api/creator/system/localization');
      const { data } = await response.json();
      setLanguages(data.languages);
      setTimezones(data.timezones);
    };
    fetchLiterals();
  }, []);

  // Fetch current settings
  useEffect(() => {
    const fetchSettings = async () => {
      const response = await fetch('/api/creator/settings/language', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const { data } = await response.json();
      setSelectedLanguage(data.language);
      setSelectedTimezone(data.timezone);
    };
    fetchSettings();
  }, []);

  const handleSave = async () => {
    await fetch('/api/creator/settings/language', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        language: selectedLanguage,
        timezone: selectedTimezone
      })
    });
  };

  return (
    <div>
      <h2>Language Settings</h2>
      
      <label>Language</label>
      <select 
        value={selectedLanguage} 
        onChange={(e) => setSelectedLanguage(e.target.value)}
      >
        {languages.map(lang => (
          <option key={lang.code} value={lang.code}>
            {lang.label}
          </option>
        ))}
      </select>

      <label>Timezone</label>
      <select 
        value={selectedTimezone} 
        onChange={(e) => setSelectedTimezone(e.target.value)}
      >
        {timezones.map(tz => (
          <option key={tz} value={tz}>
            {tz}
          </option>
        ))}
      </select>

      <button onClick={handleSave}>Save Settings</button>
    </div>
  );
}
```

#### 2. Rate Configuration UI

```jsx
import { useState, useEffect } from 'react';

function RateSettings() {
  const [rateConfig, setRateConfig] = useState({
    hourlyRate: 0,
    dailyRate: 0,
    projectRate: 0,
    currency: 'USDC',
    minimumBudget: 0,
    selected: 'hourly'
  });

  // Fetch current rate configuration
  useEffect(() => {
    const fetchRate = async () => {
      const response = await fetch('/api/creator/settings/rate', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const { data } = await response.json();
      setRateConfig(data);
    };
    fetchRate();
  }, []);

  const handleChange = (field, value) => {
    setRateConfig(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSave = async () => {
    const response = await fetch('/api/creator/settings/rate', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(rateConfig)
    });
    const result = await response.json();
    if (result.success) {
      alert('Rate configuration updated successfully');
    }
  };

  return (
    <div>
      <h2>Rate Configuration</h2>

      <div>
        <h3>Select Pricing Model</h3>
        <label>
          <input 
            type="radio" 
            value="hourly"
            checked={rateConfig.selected === 'hourly'}
            onChange={(e) => handleChange('selected', e.target.value)}
          />
          Hourly Rate
        </label>
        <label>
          <input 
            type="radio" 
            value="daily"
            checked={rateConfig.selected === 'daily'}
            onChange={(e) => handleChange('selected', e.target.value)}
          />
          Daily Rate
        </label>
        <label>
          <input 
            type="radio" 
            value="project"
            checked={rateConfig.selected === 'project'}
            onChange={(e) => handleChange('selected', e.target.value)}
          />
          Project Rate
        </label>
      </div>

      <div>
        <label>Hourly Rate</label>
        <input 
          type="number"
          value={rateConfig.hourlyRate}
          onChange={(e) => handleChange('hourlyRate', parseFloat(e.target.value))}
          disabled={rateConfig.selected !== 'hourly'}
        />
      </div>

      <div>
        <label>Daily Rate</label>
        <input 
          type="number"
          value={rateConfig.dailyRate}
          onChange={(e) => handleChange('dailyRate', parseFloat(e.target.value))}
          disabled={rateConfig.selected !== 'daily'}
        />
      </div>

      <div>
        <label>Project Rate</label>
        <input 
          type="number"
          value={rateConfig.projectRate}
          onChange={(e) => handleChange('projectRate', parseFloat(e.target.value))}
          disabled={rateConfig.selected !== 'project'}
        />
      </div>

      <div>
        <label>Currency</label>
        <input 
          type="text"
          value={rateConfig.currency}
          onChange={(e) => handleChange('currency', e.target.value)}
        />
      </div>

      <div>
        <label>Minimum Budget</label>
        <input 
          type="number"
          value={rateConfig.minimumBudget}
          onChange={(e) => handleChange('minimumBudget', parseFloat(e.target.value))}
        />
      </div>

      <button onClick={handleSave}>Save Configuration</button>
    </div>
  );
}
```

---

## Backend Implementation Notes

### Localization Endpoint

**Static Data:**
The localization literals should be returned as static data. No database query needed.

```javascript
// Backend implementation example
app.get('/api/creator/system/localization', (req, res) => {
  res.json({
    success: true,
    data: {
      languages: [
        { code: "en", label: "English" },
        { code: "en-US", label: "English (United States)" },
        { code: "zh-CN", label: "Chinese (Simplified, China)" },
        { code: "zh-TW", label: "Chinese (Traditional, Taiwan)" },
        { code: "ja-JP", label: "Japanese (Japan)" },
        { code: "ko-KR", label: "Korean (South Korea)" }
      ],
      timezones: [
        "America/New_York",
        "Europe/London",
        "Asia/Shanghai",
        "Asia/Tokyo",
        "Asia/Seoul"
      ]
    }
  });
});
```

**Extensibility:**
To add more languages or timezones, simply update the static arrays.

### Rate Configuration Endpoint

**Database Schema:**
```sql
CREATE TABLE creator_rate_settings (
  creator_id UUID PRIMARY KEY REFERENCES users(id),
  hourly_rate DECIMAL(10, 2) DEFAULT 0,
  daily_rate DECIMAL(10, 2) DEFAULT 0,
  project_rate DECIMAL(10, 2) DEFAULT 0,
  currency VARCHAR(10) DEFAULT 'USDC',
  minimum_budget DECIMAL(10, 2) DEFAULT 0,
  selected VARCHAR(10) CHECK (selected IN ('hourly', 'daily', 'project')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Validation:**
```javascript
const validateRateConfig = (data) => {
  // Validate selected field
  if (!['hourly', 'daily', 'project'].includes(data.selected)) {
    throw new Error('Invalid selected value. Must be: hourly, daily, or project');
  }

  // Validate rates are non-negative
  if (data.hourlyRate < 0 || data.dailyRate < 0 || data.projectRate < 0) {
    throw new Error('Rates must be non-negative');
  }

  // Validate currency
  if (!data.currency || data.currency.length === 0) {
    throw new Error('Currency is required');
  }

  return true;
};
```

---

## Testing Checklist

### Localization Endpoint
- [ ] GET /creator/system/localization returns correct structure
- [ ] Languages array contains all expected languages
- [ ] Each language has code and label
- [ ] Timezones array contains all expected timezones
- [ ] No authentication required
- [ ] Response is cacheable

### Rate Configuration
- [ ] GET /creator/settings/rate requires authentication
- [ ] GET returns all rate fields including selected
- [ ] POST with valid data updates configuration
- [ ] POST validates selected enum values
- [ ] POST rejects invalid selected values
- [ ] Selected field accepts: hourly, daily, project
- [ ] Selected field rejects other values
- [ ] Currency field defaults to USDC
- [ ] Rates default to 0

---

## Summary

Both features are fully implemented:

1. **Localization Endpoint**: GET /creator/system/localization provides language and timezone literals for UI dropdowns
2. **Rate Configuration**: Includes "selected" field with enum values (hourly, daily, project) for both GET and POST endpoints

No changes needed - the API specification already matches your requirements exactly.
