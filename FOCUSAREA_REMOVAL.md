# focusArea Field Removal from Project Campaigns

## Change Summary

**Date:** December 16, 2025  
**Reason:** The focusArea field does not exist when creating campaigns from the Project Portal, so it has been removed from all project campaign-related schemas.

## Changes Made

### 1. CampaignCreateRequest Schema

**Location:** Line 1425-1475

**Removed from required fields:**
- focusArea

**Removed from properties:**
- focusArea field definition

**Before:**
```yaml
required:
  - name
  - focusArea
  - description
  - durationType
  # ... other fields

properties:
  name:
    type: string
  focusArea:
    $ref: '#/components/schemas/FocusArea'
  description:
    type: string
```

**After:**
```yaml
required:
  - name
  - description
  - durationType
  # ... other fields

properties:
  name:
    type: string
  description:
    type: string
```

### 2. CampaignDetail Schema

**Location:** Line 678-768

**Removed from properties:**
- focusArea field definition

**Before:**
```yaml
properties:
  objective:
    type: string
    description: Explicit campaign objective description
  focusArea:
    $ref: '#/components/schemas/FocusArea'
  status:
    $ref: '#/components/schemas/CampaignStatus'
```

**After:**
```yaml
properties:
  objective:
    type: string
    description: Explicit campaign objective description
  status:
    $ref: '#/components/schemas/CampaignStatus'
```

### 3. ProjectCampaignListItem Schema

**Location:** Line 1541-1587

**Removed from required fields:**
- focusArea

**Removed from properties:**
- focusArea field definition

**Before:**
```yaml
required:
  - campaignId
  - name
  - focusArea
  - status
  - budgetTotal
  - budgetRemaining

properties:
  campaignId:
    type: string
  name:
    type: string
  focusArea:
    $ref: '#/components/schemas/FocusArea'
  status:
    $ref: '#/components/schemas/CampaignStatus'
```

**After:**
```yaml
required:
  - campaignId
  - name
  - status
  - budgetTotal
  - budgetRemaining

properties:
  campaignId:
    type: string
  name:
    type: string
  status:
    $ref: '#/components/schemas/CampaignStatus'
```

## Affected Endpoints

### 1. POST /project/campaigns (Create Campaign)
- Uses: CampaignCreateRequest
- Impact: focusArea is no longer required or accepted in the request body

**Old Request Example:**
```json
{
  "name": "DeFi Protocol Launch",
  "focusArea": 1,
  "description": "Campaign description",
  "durationType": 2,
  "startDate": "2025-01-01",
  "creatorBudget": 10000,
  "numberOfCreators": 3
}
```

**New Request Example:**
```json
{
  "name": "DeFi Protocol Launch",
  "description": "Campaign description",
  "durationType": 2,
  "startDate": "2025-01-01",
  "creatorBudget": 10000,
  "numberOfCreators": 3
}
```

### 2. GET /project/campaigns/{id} (Get Campaign Details)
- Uses: CampaignDetail
- Impact: focusArea is no longer included in the response

**Old Response Example:**
```json
{
  "success": true,
  "data": {
    "campaignId": "uuid",
    "projectId": "uuid",
    "title": "DeFi Protocol Launch",
    "description": "Campaign description",
    "focusArea": 1,
    "status": 3,
    "budgetAmount": 10000
  }
}
```

**New Response Example:**
```json
{
  "success": true,
  "data": {
    "campaignId": "uuid",
    "projectId": "uuid",
    "title": "DeFi Protocol Launch",
    "description": "Campaign description",
    "status": 3,
    "budgetAmount": 10000
  }
}
```

### 3. GET /project/campaigns (List Project Campaigns)
- Uses: ProjectCampaignListItem
- Impact: focusArea is no longer included in the campaign list items

**Old Response Example:**
```json
{
  "success": true,
  "data": {
    "campaigns": [
      {
        "campaignId": "uuid",
        "name": "DeFi Protocol Launch",
        "focusArea": 1,
        "status": 3,
        "budgetTotal": 10000,
        "budgetRemaining": 5000
      }
    ]
  }
}
```

**New Response Example:**
```json
{
  "success": true,
  "data": {
    "campaigns": [
      {
        "campaignId": "uuid",
        "name": "DeFi Protocol Launch",
        "status": 3,
        "budgetTotal": 10000,
        "budgetRemaining": 5000
      }
    ]
  }
}
```

## Important Notes

### What This Change Does NOT Affect

The focusArea field still exists in:
- **Creator Campaign endpoints** (GET /creator/campaigns) - Creators use vertical parameter for filtering, but individual campaigns may still have focus area information if needed
- **CampaignListItem schema** - Used by creator campaign list endpoint
- **FocusArea enum definition** - Still exists for other purposes

### Rationale

The focusArea field was removed from project campaign schemas because:
1. Project Portal UI does not include a focusArea field when creating campaigns
2. The field was not being collected during campaign creation
3. Including it in the API spec created confusion between API documentation and actual UI behavior
4. Projects can have multiple focus areas across different campaigns, so focus area is not a campaign-level attribute from the project perspective

### Migration Guide

#### For Frontend Developers

**If you were sending focusArea in campaign creation:**
```javascript
// OLD - Will fail validation
const createCampaign = async () => {
  const response = await fetch('/api/project/campaigns', {
    method: 'POST',
    body: JSON.stringify({
      name: 'Campaign Name',
      focusArea: 1,  // Remove this
      description: 'Description'
      // ... other fields
    })
  });
};

// NEW - Correct format
const createCampaign = async () => {
  const response = await fetch('/api/project/campaigns', {
    method: 'POST',
    body: JSON.stringify({
      name: 'Campaign Name',
      description: 'Description'
      // ... other fields
      // focusArea not included
    })
  });
};
```

**If you were reading focusArea from campaign details:**
```javascript
// OLD - focusArea will be undefined
const { data: campaign } = await response.json();
const focusArea = campaign.focusArea;  // undefined

// NEW - focusArea removed
const { data: campaign } = await response.json();
// Don't try to access focusArea
// Use other campaign fields instead
```

#### For Backend Developers

**Campaign creation validation:**
- Remove focusArea from required field validation
- Remove focusArea from database INSERT statements for project campaigns
- Remove focusArea from any campaign creation business logic

**Campaign response serialization:**
- Remove focusArea from response objects for project campaign endpoints
- Update database queries to not select focusArea for project campaigns

## Testing Checklist

- [ ] Campaign creation without focusArea succeeds
- [ ] Campaign creation with focusArea (if sent) is ignored or returns validation error
- [ ] GET /project/campaigns/{id} returns campaign without focusArea
- [ ] GET /project/campaigns returns list without focusArea in items
- [ ] Existing campaigns still load correctly
- [ ] Campaign filtering works without focusArea
- [ ] Campaign editing works without focusArea

## Related Documentation

- API Specification: swagger.yaml
- FocusArea enum: Still defined in components/schemas/FocusArea
- Creator Campaign endpoints: Still use vertical parameter for filtering (maps to FocusArea values)

## Summary

The focusArea field has been completely removed from:
1. Campaign creation request (CampaignCreateRequest)
2. Campaign detail response (CampaignDetail)
3. Campaign list item response (ProjectCampaignListItem)

This aligns the API specification with the actual Project Portal UI behavior where focusArea is not collected during campaign creation.
