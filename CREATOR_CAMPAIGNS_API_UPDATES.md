# Creator Campaigns API Updates - UI Alignment

**Date:** December 16, 2025  
**Endpoint:** `GET /creator/campaigns`  
**Purpose:** Align API parameters with UI interface requirements

---

## Summary of Changes

The `/creator/campaigns` endpoint has been updated to match the UI interface requirements, addressing all mismatches between the API specification and the actual UI implementation.

---

## Problems Identified and Fixed

### 1. Missing Parameters (Now Added)

#### keyword (string)
- **UI Requirement:** Search input field at top of campaigns list
- **API Status:** ADDED
- **Type:** Single string value
- **Description:** Keyword search against campaign title and description
- **Example:** `keyword=NFT marketing`

#### stage (array of integers)
- **UI Requirement:** "Stage" filter section with multi-select checkboxes
- **API Status:** ADDED
- **Type:** Array of integers [1-6]
- **Values:**
  - 1 = Ideation
  - 2 = Live Configuration
  - 3 = Notification Preferences
  - 4 = Privacy Controls
  - 5 = Wallet & Payout
  - 6 = Security
- **Example:** `stage=2&stage=3`

### 2. Parameter Name Changes

#### focusArea → vertical
- **Old:** `focusArea` (single integer)
- **New:** `vertical` (array of integers)
- **Reason:** Match UI terminology and support multi-select
- **UI Requirement:** "Vertical" filter section with multi-select checkboxes
- **Values:** [1-9]
  - 1 = DeFi
  - 2 = NFT
  - 3 = Gaming
  - 4 = Infrastructure
  - 5 = L2
  - 6 = DAO
  - 7 = Metaverse
  - 8 = Trading
  - 9 = Other
- **Example:** `vertical=2&vertical=3`

#### deliverableType → deliverables
- **Old:** `deliverableType` (single integer)
- **New:** `deliverables` (array of integers)
- **Reason:** Support multi-select as shown in UI
- **UI Requirement:** "Deliverables" filter section with multi-select checkboxes
- **Values:** [1-8]
  - 1 = Twitter Posts
  - 2 = Videos
  - 3 = Articles
  - 4 = AMAs
  - 5 = Discord Management
  - 6 = Community Giveaways
  - 7 = Instagram Post
  - 8 = TikTok
- **Example:** `deliverables=1&deliverables=2`

### 3. Type Changes (Single to Array)

#### duration
- **Old:** Single integer
- **New:** Array of integers
- **Reason:** Support multi-select as shown in UI
- **UI Requirement:** "Duration" filter section with multi-select checkboxes
- **Values:** [1-5]
  - 1 = Less Than 1 Week
  - 2 = 1-2 Weeks
  - 3 = 2-4 Weeks
  - 4 = 1-3 Months
  - 5 = 3+ Months
- **Example:** `duration=1&duration=2`

### 4. Removed Parameters

The following parameters were present in the old API but are not shown in the UI:

#### minBudget (removed)
- **Reason:** Not present in UI filter interface

#### maxBudget (removed)
- **Reason:** Not present in UI filter interface

#### complexity (removed)
- **Reason:** Not present in UI filter interface

**Note:** These parameters can be re-added in the future if budget and complexity filters are added to the UI.

---

## Updated Parameter List

### Current Parameters (All UI-Aligned)

| Parameter | Type | Required | Multi-Select | Description |
|-----------|------|----------|--------------|-------------|
| `keyword` | string | No | No | Keyword search (title/description) |
| `vertical` | array of integers | No | Yes | Focus areas (1-9) |
| `deliverables` | array of integers | No | Yes | Deliverable types (1-8) |
| `duration` | array of integers | No | Yes | Campaign duration (1-5) |
| `stage` | array of integers | No | Yes | Campaign stage (1-6) |
| `page` | integer | No | No | Page number (zero-indexed, default: 0) |
| `size` | integer | No | No | Items per page (default: 20, max: 100) |

---

## API Request Examples

### Example 1: Basic Search
```
GET /api/creator/campaigns?keyword=NFT
```

### Example 2: Filter by Vertical (Multi-Select)
```
GET /api/creator/campaigns?vertical=2&vertical=3
(NFT and Gaming)
```

### Example 3: Filter by Deliverables (Multi-Select)
```
GET /api/creator/campaigns?deliverables=1&deliverables=2
(Twitter Posts and Videos)
```

### Example 4: Complex Filter Query
```
GET /api/creator/campaigns?keyword=DeFi&vertical=1&vertical=2&deliverables=1&deliverables=2&duration=1&duration=2&stage=2&stage=3&page=0&size=20
```

**Explanation:**
- Keyword: "DeFi"
- Vertical: DeFi (1) and NFT (2)
- Deliverables: Twitter Posts (1) and Videos (2)
- Duration: Less Than 1 Week (1) and 1-2 Weeks (2)
- Stage: Live Configuration (2) and Notification Preferences (3)
- Pagination: Page 0, 20 items per page

---

## Query Parameter Format

### Multi-Select Parameters

The API supports two formats for multi-select parameters:

#### Format 1: Repeated Parameters (Recommended)
```
?vertical=1&vertical=2&vertical=3
```

#### Format 2: Comma-Separated (Alternative)
```
?vertical=1,2,3
```

Both formats are equivalent and will be processed correctly by the backend.

**OpenAPI Specification:**
- `style: form`
- `explode: true`

This configuration tells the API to accept repeated parameter names for arrays.

---

## Schema Definitions

### Enums Used

All filter parameters reference existing enum schemas:

#### FocusArea Enum
```yaml
type: integer
enum: [1, 2, 3, 4, 5, 6, 7, 8, 9]
1 = DeFi, 2 = NFT, 3 = Gaming, 4 = Infrastructure, 
5 = L2, 6 = DAO, 7 = Metaverse, 8 = Trading, 9 = Other
```

#### DeliverableType Enum
```yaml
type: integer
enum: [1, 2, 3, 4, 5, 6, 7, 8]
1 = Twitter Posts, 2 = Videos, 3 = Articles, 4 = AMAs,
5 = Discord Management, 6 = Community Giveaways,
7 = Instagram Post, 8 = TikTok
```

#### CampaignDuration Enum
```yaml
type: integer
enum: [1, 2, 3, 4, 5]
1 = Less Than 1 Week, 2 = 1-2 Weeks, 3 = 2-4 Weeks,
4 = 1-3 Months, 5 = 3+ Months
```

#### CampaignStage Enum (New)
```yaml
type: integer
enum: [1, 2, 3, 4, 5, 6]
1 = Ideation, 2 = Live Configuration,
3 = Notification Preferences, 4 = Privacy Controls,
5 = Wallet & Payout, 6 = Security
```

---

## Frontend Implementation Guide

### React Example with URL Search Params

```typescript
// Building the query URL
const buildCampaignsQuery = (filters: CampaignFilters) => {
  const params = new URLSearchParams();
  
  // Keyword (single value)
  if (filters.keyword) {
    params.append('keyword', filters.keyword);
  }
  
  // Vertical (multi-select)
  if (filters.vertical && filters.vertical.length > 0) {
    filters.vertical.forEach(v => params.append('vertical', v.toString()));
  }
  
  // Deliverables (multi-select)
  if (filters.deliverables && filters.deliverables.length > 0) {
    filters.deliverables.forEach(d => params.append('deliverables', d.toString()));
  }
  
  // Duration (multi-select)
  if (filters.duration && filters.duration.length > 0) {
    filters.duration.forEach(d => params.append('duration', d.toString()));
  }
  
  // Stage (multi-select)
  if (filters.stage && filters.stage.length > 0) {
    filters.stage.forEach(s => params.append('stage', s.toString()));
  }
  
  // Pagination
  params.append('page', filters.page.toString());
  params.append('size', filters.size.toString());
  
  return `/api/creator/campaigns?${params.toString()}`;
};

// Example usage
const filters = {
  keyword: 'NFT',
  vertical: [2, 3],  // NFT, Gaming
  deliverables: [1, 2],  // Twitter Posts, Videos
  duration: [1, 2],  // Less Than 1 Week, 1-2 Weeks
  stage: [2, 3],  // Live Configuration, Notification Preferences
  page: 0,
  size: 20
};

const url = buildCampaignsQuery(filters);
// Result: /api/creator/campaigns?keyword=NFT&vertical=2&vertical=3&deliverables=1&deliverables=2&duration=1&duration=2&stage=2&stage=3&page=0&size=20

fetch(url, {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(res => res.json())
.then(data => console.log(data.data.campaigns));
```

---

## Backend Implementation Notes

### Query Parameter Parsing

The backend should parse array parameters correctly. Most frameworks handle this automatically:

#### Express.js Example
```javascript
app.get('/api/creator/campaigns', async (req, res) => {
  const {
    keyword,
    vertical,      // Already an array if multiple values sent
    deliverables,  // Already an array if multiple values sent
    duration,      // Already an array if multiple values sent
    stage,         // Already an array if multiple values sent
    page = 0,
    size = 20
  } = req.query;
  
  // Convert to arrays if single value received
  const verticalArray = Array.isArray(vertical) ? vertical : (vertical ? [vertical] : []);
  const deliverablesArray = Array.isArray(deliverables) ? deliverables : (deliverables ? [deliverables] : []);
  const durationArray = Array.isArray(duration) ? duration : (duration ? [duration] : []);
  const stageArray = Array.isArray(stage) ? stage : (stage ? [stage] : []);
  
  // Build database query
  const campaigns = await db.query(`
    SELECT * FROM campaigns
    WHERE 
      ($1::text IS NULL OR title ILIKE '%' || $1 || '%' OR description ILIKE '%' || $1 || '%')
      AND ($2::int[] IS NULL OR focus_area = ANY($2::int[]))
      AND ($3::int[] IS NULL OR deliverable_type = ANY($3::int[]))
      AND ($4::int[] IS NULL OR duration = ANY($4::int[]))
      AND ($5::int[] IS NULL OR stage = ANY($5::int[]))
    LIMIT $6 OFFSET $7
  `, [
    keyword || null,
    verticalArray.length > 0 ? verticalArray : null,
    deliverablesArray.length > 0 ? deliverablesArray : null,
    durationArray.length > 0 ? durationArray : null,
    stageArray.length > 0 ? stageArray : null,
    size,
    page * size
  ]);
  
  res.json({ success: true, data: { campaigns } });
});
```

---

## Migration Guide

### For Existing Frontend Code

If you were using the old API parameters, here's how to migrate:

#### Old Code
```typescript
// Old parameter names (single values)
const url = `/api/creator/campaigns?focusArea=2&deliverableType=1&duration=1&page=0&size=20`;
```

#### New Code
```typescript
// New parameter names (arrays)
const url = `/api/creator/campaigns?vertical=2&deliverables=1&duration=1&page=0&size=20`;
```

#### Migration Checklist
- [ ] Replace `focusArea` with `vertical`
- [ ] Replace `deliverableType` with `deliverables`
- [ ] Remove `minBudget` and `maxBudget` (if used)
- [ ] Remove `complexity` (if used)
- [ ] Add `keyword` parameter (if search functionality needed)
- [ ] Add `stage` parameter (if stage filtering needed)
- [ ] Update all parameters to support array values (multi-select)

---

## Testing Checklist

### Parameter Testing
- [ ] Keyword search returns filtered results
- [ ] Vertical filter with single value works
- [ ] Vertical filter with multiple values works
- [ ] Deliverables filter with single value works
- [ ] Deliverables filter with multiple values works
- [ ] Duration filter with single value works
- [ ] Duration filter with multiple values works
- [ ] Stage filter with single value works
- [ ] Stage filter with multiple values works
- [ ] Pagination works correctly
- [ ] Combining all filters works correctly

### Edge Cases
- [ ] Empty filters return all campaigns
- [ ] Invalid enum values are rejected (400 error)
- [ ] Pagination beyond available results returns empty array
- [ ] Large page size (>100) is rejected or capped
- [ ] Special characters in keyword search are handled correctly

---

## Related Endpoints

**Note:** Other campaign endpoints (public marketplace, project campaigns) still use different parameter structures. These may need separate updates based on their respective UI requirements:

- `/public/marketplace/campaigns` - Still uses `focusArea` (single value), `minBudget`, `maxBudget`
- `/project/campaigns` - Still uses `focusArea` (single value), `minBudget`, `maxBudget`

These endpoints serve different UIs and may have different filtering requirements. Update them separately if needed.

---

## Documentation Updates

The following sections in the OpenAPI specification have been updated:

1. **Endpoint Description:** Added comprehensive UI alignment notes and parameter mappings
2. **Parameter Descriptions:** Each parameter now includes:
   - UI location reference
   - Complete enum value mappings
   - Multi-select support indication
   - Examples
3. **Parameter Specifications:** 
   - Added `required: false` explicitly
   - Added `style: form` and `explode: true` for arrays
   - Added examples for all parameters
   - Added min/max constraints for pagination

---

## Summary

The `/creator/campaigns` endpoint is now fully aligned with the UI interface requirements:

**Added:**
- keyword search
- stage filter
- Multi-select support for all filters

**Renamed:**
- focusArea → vertical
- deliverableType → deliverables

**Removed:**
- minBudget
- maxBudget
- complexity

**Enhanced:**
- Comprehensive documentation
- Clear enum mappings
- UI-aligned terminology
- Multi-select array support

All changes are backward-incompatible with the old API structure. Ensure frontend code is updated accordingly before deploying.
