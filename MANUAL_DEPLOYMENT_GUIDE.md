# Manual Deployment Guide - Project Portal API

## Current Situation

**Status**: Project Portal API is implemented locally but NOT deployed to Render

**Problem**: Network connectivity issues prevent automated git push to GitHub

**Local Commits**: 2 unpushed commits containing Project Portal implementation:
- `ecb694d` - Add deployment instructions
- `7bdabdd` - Add comprehensive Project Portal API (30+ endpoints)

**GitHub Repository**: Missing the Project Portal commits
- Current: Only has `a89df82` (Creator Portal improvements)
- Needed: Add `7bdabdd` and `ecb694d`

**Render Deployment**: Currently deploying from old commit `a89df82`
- Missing: All Project Portal endpoints
- Missing: Admin Portal endpoints

## Solution: Manual Push via GitHub Desktop

### Step-by-Step Instructions

#### 1. Install GitHub Desktop (if not already installed)

Download from: https://desktop.github.com/

#### 2. Open GitHub Desktop

Launch the application

#### 3. Add Your Local Repository

**Option A: If repository is already added**
- Click on "Current Repository" dropdown (top left)
- Select `aw3-platform-mock-api`

**Option B: If repository is not added**
- File > Add Local Repository
- Click "Choose..." button
- Navigate to: `A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api`
- Click "Add Repository"

#### 4. Verify Remote Repository

- Repository menu > Repository Settings
- Under "Primary remote repository (origin)", verify it shows:
  ```
  https://github.com/Allweb3Labs/aw3-platform-mock-api.git
  ```
- If incorrect, click "Change remote" and enter the correct URL

#### 5. Push Commits

- You should see "2 commits to push" or similar message
- Click the blue "Push origin" button
- Wait for push to complete (usually 5-10 seconds)

#### 6. Verify Push Success

**In GitHub Desktop**:
- Check that no commits are pending
- Status should show "Last fetched just now"

**In Browser**:
- Visit: https://github.com/Allweb3Labs/aw3-platform-mock-api/commits/main
- Verify you see these commits:
  1. "Add deployment instructions"
  2. "Add comprehensive Project Portal API with dashboard, campaigns..."

#### 7. Monitor Render Deployment

**Automatic Deployment** (Render will detect GitHub changes)

1. Visit: https://dashboard.render.com/web/srv-d4mi7kili9vc73ercdn0

2. You should see a new deployment starting with:
   - Commit: `7bdabdd` or `ecb694d`
   - Status: "building" 

3. Wait 3-5 minutes for deployment to complete

4. Status should change from "building" to "live"

#### 8. Verify Project Portal Endpoints

**Test via Swagger UI**:
- Visit: https://aw3-platform-mock-api.onrender.com/docs
- Verify these new sections appear:
  - Project Dashboard
  - Project Campaigns
  - Project Applications
  - Project Deliverables
  - Project Creators
  - Project Analytics
  - Project Finance
  - Project Wallet

**Test via API**:
```bash
# Test Project Dashboard
curl https://aw3-platform-mock-api.onrender.com/api/project/dashboard/stats

# Expected: JSON response with dashboard statistics

# Test Project Campaigns
curl https://aw3-platform-mock-api.onrender.com/api/project/campaigns

# Expected: JSON response with campaign list

# Test Creator Discovery
curl https://aw3-platform-mock-api.onrender.com/api/project/creators/discover

# Expected: JSON response with creator profiles
```

## Alternative Method: Visual Studio Code

If you prefer using VS Code:

### 1. Open VS Code
- Open folder: `A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api`

### 2. Open Source Control
- Click on Source Control icon (left sidebar)
- Or press: `Ctrl+Shift+G`

### 3. Verify Changes
- You should see 2 commits ready to push
- If not visible, click the "..." menu > Push

### 4. Push to GitHub
- Click on "..." (three dots) in Source Control panel
- Select "Push"
- Or press: `Ctrl+Shift+P`, type "Git: Push", press Enter

### 5. Follow Steps 6-8 from GitHub Desktop method above

## Troubleshooting

### Issue: GitHub Desktop shows "Authentication failed"

**Solution**:
1. File > Options > Accounts
2. Sign out of GitHub
3. Sign in again
4. Try push again

### Issue: Push fails with network error

**Solution A - Use SSH**:
```powershell
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
git remote set-url origin git@github.com:Allweb3Labs/aw3-platform-mock-api.git
git push origin main
```

**Solution B - Use VPN**:
1. Connect to a VPN service
2. Retry push operation

**Solution C - Use Git Bash**:
1. Right-click in folder > "Git Bash Here"
2. Run: `git push origin main`
3. May prompt for credentials - use GitHub token

### Issue: Render doesn't automatically deploy

**Solution - Manual Deploy**:
1. Go to: https://dashboard.render.com/web/srv-d4mi7kili9vc73ercdn0
2. Click "Manual Deploy" button
3. Select "Deploy latest commit"
4. Click "Deploy"

### Issue: New endpoints return 404

**Causes**:
1. Deployment still in progress (wait 2-3 more minutes)
2. Deployment failed (check Render logs)
3. Wrong URL (verify you're using .onrender.com, not localhost)

**Solution**:
1. Check deployment status: https://dashboard.render.com/web/srv-d4mi7kili9vc73ercdn0
2. Wait for status to show "live"
3. Clear browser cache
4. Test in incognito/private window

## What Gets Deployed

### New Project Portal Endpoints (30+)

**Project Dashboard**:
- GET `/api/project/dashboard/stats`

**Project Campaigns** (15 endpoints):
- GET `/api/project/campaigns` - List with filters
- POST `/api/project/campaigns` - Create campaign
- GET `/api/project/campaigns/{id}` - Get details
- PUT `/api/project/campaigns/{id}` - Update
- DELETE `/api/project/campaigns/{id}` - Delete draft
- POST `/api/project/campaigns/{id}/pause` - Pause
- POST `/api/project/campaigns/{id}/resume` - Resume
- POST `/api/project/campaigns/{id}/extend` - Extend deadline
- POST `/api/project/campaigns/{id}/invite` - Invite creators
- GET `/api/project/campaigns/{id}/export` - Export report
- GET `/api/project/campaigns/{id}/metrics` - Get metrics
- GET `/api/project/campaigns/{id}/overview` - Get overview
- GET `/api/project/campaigns/{id}/analytics` - Get analytics
- GET `/api/project/campaigns/{id}/financials` - Get financials

**Project Applications** (5 endpoints):
- GET `/api/project/applications` - List with filters
- GET `/api/project/applications/{id}` - Get details
- POST `/api/project/applications/approve` - Batch approve
- POST `/api/project/applications/reject` - Batch reject
- GET `/api/project/applications/{id}/deliverables`

**Project Deliverables** (5 endpoints):
- GET `/api/project/deliverables` - List with filters
- GET `/api/project/deliverables/{id}` - Get details
- POST `/api/project/deliverables/{id}/verify` - Verify
- POST `/api/project/deliverables/{id}/request-revision`
- POST `/api/project/deliverables/{id}/reject`

**Project Creators** (3 endpoints):
- GET `/api/project/creators/discover` - CVPI-focused search
- GET `/api/project/creators/recommended` - AI recommendations
- GET `/api/project/creators/{id}` - Get details

**Project Analytics**:
- GET `/api/project/analytics/overview`

**Project Finance**:
- GET `/api/project/finance/summary`
- GET `/api/project/finance/budget-allocation`

**Project Wallet**:
- GET `/api/project/wallet/overview`
- GET `/api/project/wallet/escrow`
- GET `/api/project/wallet/transactions`
- POST `/api/project/wallet/deposit`
- POST `/api/project/wallet/withdraw`

### New Schemas (30+)

Including:
- `ProjectDashboardStats`
- `ProjectCampaign`
- `ProjectApplication`
- `ProjectDeliverable`
- `ProjectCreatorProfile`
- `ProjectAnalyticsSummary`
- `ProjectFinanceSummary`
- `ProjectWalletOverview`
- And 20+ more...

### New ENUMs (5+)

- `ProjectCampaignSort`
- `ApplicationSort`
- `PaymentStructure`
- `ApprovalMethod`
- `DurationType`

## Summary

**Total Changes**:
- Files Modified: 2 (swagger.yaml, server.js)
- Lines Added: 1,500+
- New Endpoints: 30+
- New Schemas: 30+
- New ENUMs: 5+

**Deployment Time**: 
- Push to GitHub: < 1 minute
- Render deployment: 3-5 minutes
- Total: < 10 minutes

**Final URLs**:
- **API Documentation**: https://aw3-platform-mock-api.onrender.com/docs
- **GitHub Repository**: https://github.com/Allweb3Labs/aw3-platform-mock-api
- **Render Dashboard**: https://dashboard.render.com/web/srv-d4mi7kili9vc73ercdn0

## Next Steps After Deployment

1. Update frontend to use new Project Portal endpoints
2. Test all endpoints with sample requests
3. Monitor API performance and logs
4. Document any environment-specific configurations
5. Create integration tests for critical endpoints

## Support

If you encounter any issues not covered in this guide:

1. Check Render logs: Dashboard > Logs
2. Verify GitHub repository has latest commits
3. Ensure Render is connected to correct repository
4. Try manual deployment from Render dashboard
5. Check network connectivity and firewall settings

