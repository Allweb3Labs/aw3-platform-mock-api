# API vs UI Comparison - Before and After

## Before (Mismatched)

### API Parameters (Old)
```
GET /creator/campaigns
Parameters:
- focusArea: integer (single value)
- minBudget: number
- maxBudget: number
- deliverableType: integer (single value)
- duration: integer (single value)
- complexity: integer (single value)
- page: integer
- size: integer
```

### UI Requirements
```
UI Filter Sections:
- Keyword Search: text input
- Vertical: multi-select checkboxes (DeFi, NFT, Gaming, etc.)
- Deliverables: multi-select checkboxes (Twitter Posts, Videos, etc.)
- Duration: multi-select checkboxes (Less Than 1 Week, 1-2 Weeks, etc.)
- Stage: multi-select checkboxes (Ideation, Live Configuration, etc.)
```

### Problems Identified
```
1. Missing in API:
   - keyword (search)
   - stage (filter)

2. Wrong parameter names:
   - focusArea should be "vertical" (to match UI terminology)
   - deliverableType should be "deliverables" (plural)

3. Wrong parameter types:
   - All filters use single integer values
   - UI requires multi-select (arrays)

4. Extra parameters not in UI:
   - minBudget
   - maxBudget
   - complexity
```

---

## After (Aligned)

### API Parameters (New)
```
GET /creator/campaigns
Parameters:
- keyword: string (single value)
- vertical: array of integers [1-9] (multi-select)
- deliverables: array of integers [1-8] (multi-select)
- duration: array of integers [1-5] (multi-select)
- stage: array of integers [1-6] (multi-select)
- page: integer
- size: integer
```

### UI Mapping
```
UI Section          API Parameter       Values
-----------         -------------       ------
Search Box    -->   keyword             string

Vertical      -->   vertical[]          1=DeFi, 2=NFT, 3=Gaming, 
                                       4=Infrastructure, 5=L2, 6=DAO,
                                       7=Metaverse, 8=Trading, 9=Other

Deliverables  -->   deliverables[]      1=Twitter Posts, 2=Videos,
                                       3=Articles, 4=AMAs,
                                       5=Discord Management,
                                       6=Community Giveaways,
                                       7=Instagram Post, 8=TikTok

Duration      -->   duration[]          1=Less Than 1 Week,
                                       2=1-2 Weeks, 3=2-4 Weeks,
                                       4=1-3 Months, 5=3+ Months

Stage         -->   stage[]             1=Ideation,
                                       2=Live Configuration,
                                       3=Notification Preferences,
                                       4=Privacy Controls,
                                       5=Wallet & Payout,
                                       6=Security
```

### All Issues Resolved
```
Status: All API parameters now match UI requirements

1. Added missing parameters:
   - keyword (for search)
   - stage (for stage filter)

2. Fixed parameter names:
   - focusArea renamed to vertical
   - deliverableType renamed to deliverables

3. Fixed parameter types:
   - All filters now support multi-select (arrays)

4. Removed unused parameters:
   - minBudget (not in UI)
   - maxBudget (not in UI)
   - complexity (not in UI)
```

---

## Query Examples Comparison

### Before (Old API)
```
Single value only, old parameter names:

GET /creator/campaigns?focusArea=2&deliverableType=1&duration=1&page=0&size=20

Problems:
- Can only filter by one focusArea
- Can only filter by one deliverableType
- Can only filter by one duration
- No keyword search
- No stage filter
```

### After (New API)
```
Multi-select support, correct parameter names:

GET /creator/campaigns?keyword=NFT&vertical=2&vertical=3&deliverables=1&deliverables=2&duration=1&duration=2&stage=2&stage=3&page=0&size=20

Features:
- Keyword search: "NFT"
- Multiple verticals: NFT (2) and Gaming (3)
- Multiple deliverables: Twitter Posts (1) and Videos (2)
- Multiple durations: Less Than 1 Week (1) and 1-2 Weeks (2)
- Multiple stages: Live Configuration (2) and Notification Preferences (3)
```

---

## Frontend Code Changes

### Before (Old Code)
```javascript
// Old: Single values only
const fetchCampaigns = async () => {
  const url = `/api/creator/campaigns?focusArea=2&deliverableType=1&duration=1`;
  const response = await fetch(url);
  return response.json();
};
```

### After (New Code)
```javascript
// New: Multi-select support
const fetchCampaigns = async (filters) => {
  const params = new URLSearchParams();
  
  if (filters.keyword) {
    params.append('keyword', filters.keyword);
  }
  
  filters.vertical?.forEach(v => params.append('vertical', v));
  filters.deliverables?.forEach(d => params.append('deliverables', d));
  filters.duration?.forEach(d => params.append('duration', d));
  filters.stage?.forEach(s => params.append('stage', s));
  
  params.append('page', filters.page || 0);
  params.append('size', filters.size || 20);
  
  const url = `/api/creator/campaigns?${params.toString()}`;
  const response = await fetch(url);
  return response.json();
};

// Example usage
const campaigns = await fetchCampaigns({
  keyword: 'NFT',
  vertical: [2, 3],        // NFT, Gaming
  deliverables: [1, 2],    // Twitter Posts, Videos
  duration: [1, 2],        // Less Than 1 Week, 1-2 Weeks
  stage: [2, 3],           // Live Configuration, Notification Preferences
  page: 0,
  size: 20
});
```

---

## UI Filter Behavior

### Vertical Filter (Example)
```
UI Display:
[ ] DeFi (1)
[x] NFT (2)               <- Selected
[x] Gaming (3)            <- Selected
[ ] Infrastructure (4)
[ ] L2 (5)
[ ] DAO (6)
[ ] Metaverse (7)
[ ] Trading (8)
[ ] Other (9)

API Call:
GET /creator/campaigns?vertical=2&vertical=3
```

### Deliverables Filter (Example)
```
UI Display:
[x] Twitter Posts (1)     <- Selected
[x] Videos (2)            <- Selected
[ ] Articles (3)
[ ] AMAs (4)
[ ] Discord Management (5)
[ ] Community Giveaways (6)
[ ] Instagram Post (7)
[ ] TikTok (8)

API Call:
GET /creator/campaigns?deliverables=1&deliverables=2
```

### Duration Filter (Example)
```
UI Display:
[x] Less Than 1 Week (1)  <- Selected
[x] 1-2 Weeks (2)         <- Selected
[ ] 2-4 Weeks (3)
[ ] 1-3 Months (4)
[ ] 3+ Months (5)

API Call:
GET /creator/campaigns?duration=1&duration=2
```

### Stage Filter (Example)
```
UI Display:
[ ] Ideation (1)
[x] Live Configuration (2)            <- Selected
[x] Notification Preferences (3)      <- Selected
[ ] Privacy Controls (4)
[ ] Wallet & Payout (5)
[ ] Security (6)

API Call:
GET /creator/campaigns?stage=2&stage=3
```

---

## Parameter Type Reference

### Old API
```
Parameter          Type             Multi-Select
---------          ----             ------------
focusArea          integer          NO
deliverableType    integer          NO
duration           integer          NO
complexity         integer          NO
minBudget          number           N/A
maxBudget          number           N/A
```

### New API
```
Parameter          Type             Multi-Select
---------          ----             ------------
keyword            string           NO (single search term)
vertical           integer[]        YES (array)
deliverables       integer[]        YES (array)
duration           integer[]        YES (array)
stage              integer[]        YES (array)
```

---

## Complete Enum Reference

### Vertical (FocusArea)
```
Value    Label
-----    -----
1        DeFi
2        NFT
3        Gaming
4        Infrastructure
5        L2
6        DAO
7        Metaverse
8        Trading
9        Other
```

### Deliverables (DeliverableType)
```
Value    Label
-----    -----
1        Twitter Posts
2        Videos
3        Articles
4        AMAs
5        Discord Management
6        Community Giveaways
7        Instagram Post
8        TikTok
```

### Duration (CampaignDuration)
```
Value    Label
-----    -----
1        Less Than 1 Week
2        1-2 Weeks
3        2-4 Weeks
4        1-3 Months
5        3+ Months
```

### Stage (CampaignStage)
```
Value    Label
-----    -----
1        Ideation
2        Live Configuration
3        Notification Preferences
4        Privacy Controls
5        Wallet & Payout
6        Security
```

---

## Migration Checklist

### Backend
- [ ] Update query parameter parsing to handle arrays
- [ ] Change `focusArea` to `vertical` in database queries
- [ ] Change `deliverableType` to `deliverables` in database queries
- [ ] Remove `minBudget`, `maxBudget`, `complexity` filters (if not needed)
- [ ] Add `keyword` search functionality
- [ ] Add `stage` filter functionality
- [ ] Update SQL queries to use `IN` or `ANY` for array parameters
- [ ] Test multi-select filtering logic

### Frontend
- [ ] Update API call to use new parameter names
- [ ] Change from single value to array for filters
- [ ] Implement multi-select checkbox UI
- [ ] Add keyword search input
- [ ] Add stage filter checkboxes
- [ ] Remove budget and complexity filters (if not needed)
- [ ] Update filter state management to handle arrays
- [ ] Test all filter combinations

### Testing
- [ ] Test keyword search
- [ ] Test single vertical selection
- [ ] Test multiple vertical selections
- [ ] Test single deliverable selection
- [ ] Test multiple deliverable selections
- [ ] Test single duration selection
- [ ] Test multiple duration selections
- [ ] Test single stage selection
- [ ] Test multiple stage selections
- [ ] Test combining all filters
- [ ] Test pagination with filters
- [ ] Test clearing filters

---

## Summary

**Status: API is now fully aligned with UI**

All mismatches between the API specification and UI interface have been resolved:

1. Added missing parameters (keyword, stage)
2. Renamed parameters to match UI terminology (focusArea → vertical, deliverableType → deliverables)
3. Changed single-value parameters to arrays for multi-select support
4. Removed unused parameters (minBudget, maxBudget, complexity)
5. Enhanced documentation with complete enum mappings and examples

The API now provides a 1:1 mapping to the UI filter interface, supporting all user interactions shown in the UI mockups.
