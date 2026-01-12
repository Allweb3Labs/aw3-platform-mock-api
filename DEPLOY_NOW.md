# Deploy Now - Quick Start Guide

## Current Status

The code is ready and all fixes are in place. The deployment just needs to be executed.

## Two Deployment Options

### Option 1: Direct Deployment with Vercel CLI (Fastest - 2 minutes)

This method deploys immediately without GitHub Actions.

#### Step 1: Get Your Vercel Token

1. Visit: https://vercel.com/account/tokens
2. Click "Create Token"
3. Name it: "CLI Deploy Token"
4. Click "Create"
5. Copy the token (shown only once)

#### Step 2: Deploy

Open PowerShell in the project directory and run:

```powershell
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"

# Option A: Set token as environment variable
$env:VERCEL_TOKEN = "paste_your_token_here"
.\deploy-direct.ps1

# OR Option B: Pass token as parameter
.\deploy-direct.ps1 -VercelToken "paste_your_token_here"
```

#### Step 3: Verify

After deployment completes (1-2 minutes), test:

```bash
curl https://swagger-mock-api-five.vercel.app/api/v1/demo-requests
```

Visit: https://swagger-mock-api-five.vercel.app/docs/

---

### Option 2: Configure GitHub Actions (Automatic Deployments)

This method enables automatic deployment on every push to main branch.

#### Step 1: Add GitHub Secrets

1. Go to: https://github.com/Allweb3Labs/aw3-platform-mock-api/settings/secrets/actions

2. Click "New repository secret" and add each:

**Secret 1:**
- Name: `VERCEL_TOKEN`
- Value: [Your token from https://vercel.com/account/tokens]

**Secret 2:**
- Name: `VERCEL_ORG_ID`
- Value: `team_IwX8YG1kJSayjFF4VMTQHl0K`

**Secret 3:**
- Name: `VERCEL_PROJECT_ID`
- Value: `prj_jltbOPFNuOMWAgCoWPwSqyYd1ycF`

#### Step 2: Trigger Deployment

After adding all secrets:

**Option A - Automatic:**
Wait for next push to main (or make any change and push)

**Option B - Manual:**
1. Go to: https://github.com/Allweb3Labs/aw3-platform-mock-api/actions
2. Click "Deploy to Vercel" workflow
3. Click "Run workflow"
4. Select "main" branch
5. Click "Run workflow" button

#### Step 3: Monitor

1. Watch progress in GitHub Actions tab
2. Check Vercel dashboard: https://vercel.com/allweb3labs/swagger-mock-api
3. Wait for green checkmark (2-3 minutes)

#### Step 4: Verify

```bash
curl https://swagger-mock-api-five.vercel.app/api/v1/demo-requests
```

Visit: https://swagger-mock-api-five.vercel.app/docs/

---

## What Gets Deployed

The new GET endpoint for retrieving demo requesters list:

**Endpoint**: `GET /api/v1/demo-requests`

**Query Parameters**:
- `page` - Page number (default: 0)
- `size` - Items per page (default: 20, max: 100)

**Example Requests**:
```bash
# Get first page
curl https://swagger-mock-api-five.vercel.app/api/v1/demo-requests

# Get specific page with custom size
curl "https://swagger-mock-api-five.vercel.app/api/v1/demo-requests?page=1&size=10"

# Get all on one page (if less than 100 total)
curl "https://swagger-mock-api-five.vercel.app/api/v1/demo-requests?size=100"
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "requesters": [
      {
        "requestId": "req_xxx",
        "email": "user@example.com",
        "userType": "creator",
        "socialHandle": "handle",
        "socialPlatform": "telegram",
        "source": "homepage",
        "ipAddress": "xxx.xxx.xxx.xxx",
        "createdAt": "2026-01-12T..."
      }
    ],
    "pagination": {
      "currentPage": 0,
      "pageSize": 20,
      "totalElements": 42,
      "totalPages": 3
    }
  },
  "timestamp": "2026-01-12T..."
}
```

---

## Verification Checklist

After deployment, verify these items:

- [ ] API health check responds: https://swagger-mock-api-five.vercel.app/health
- [ ] New GET endpoint works: https://swagger-mock-api-five.vercel.app/api/v1/demo-requests
- [ ] Swagger UI loads: https://swagger-mock-api-five.vercel.app/docs/
- [ ] "Request Demo" section shows GET endpoint in Swagger UI
- [ ] "Try it out" works in Swagger UI
- [ ] Pagination parameters work (test with ?page=0&size=5)
- [ ] Response includes pagination metadata

---

## Troubleshooting

### Deployment fails with "Unauthorized"
- Verify Vercel token is valid and not expired
- Check token has access to swagger-mock-api project

### Deployment fails with "Project not found"
- Verify VERCEL_PROJECT_ID is correct
- Ensure VERCEL_ORG_ID matches your team

### Endpoint returns 404 after deployment
- Wait 30-60 seconds for deployment to propagate
- Clear browser cache or use incognito mode
- Check Vercel deployment logs for errors

### Swagger UI doesn't show new endpoint
- Verify swagger.json was regenerated (check file timestamp)
- Clear browser cache
- Check swagger.json directly: https://swagger-mock-api-five.vercel.app/swagger.json

---

## Quick Command Reference

```powershell
# Navigate to project
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"

# Convert swagger files
node convert-swagger.js

# Deploy directly
.\deploy-direct.ps1 -VercelToken "YOUR_TOKEN"

# Test endpoint after deployment
curl https://swagger-mock-api-five.vercel.app/api/v1/demo-requests

# Test with pagination
curl "https://swagger-mock-api-five.vercel.app/api/v1/demo-requests?page=0&size=5"

# View swagger documentation
start https://swagger-mock-api-five.vercel.app/docs/
```

---

## Recommendation

**Use Option 1 (Direct Deployment)** for immediate results, then configure Option 2 (GitHub Actions) for future automatic deployments.

Total time: 5 minutes for Option 1, 10 minutes for both options combined.

---

## Support

- Vercel Dashboard: https://vercel.com/allweb3labs/swagger-mock-api
- GitHub Actions: https://github.com/Allweb3Labs/aw3-platform-mock-api/actions
- API Documentation: https://swagger-mock-api-five.vercel.app/docs/
- Repository: https://github.com/Allweb3Labs/aw3-platform-mock-api

For issues, check:
1. Vercel deployment logs in dashboard
2. GitHub Actions workflow logs
3. Browser console for client-side errors
4. Network tab for API response details
