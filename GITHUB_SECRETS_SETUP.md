# GitHub Secrets Setup for Vercel Deployment

## Issue

The GitHub Actions workflow for deploying to Vercel is failing because required secrets are not configured in the repository settings.

## Required Secrets

You need to add the following secrets to your GitHub repository:

### 1. VERCEL_TOKEN
Your Vercel authentication token.

**How to get it:**
1. Go to https://vercel.com/account/tokens
2. Click "Create Token"
3. Give it a name (e.g., "GitHub Actions Deploy")
4. Copy the token immediately (it will only be shown once)

### 2. VERCEL_ORG_ID
Your Vercel organization/team ID.

**Value:** `team_IwX8YG1kJSayjFF4VMTQHl0K`

(This is found in your `.vercel/project.json` file)

### 3. VERCEL_PROJECT_ID
Your Vercel project ID.

**Value:** `prj_jltbOPFNuOMWAgCoWPwSqyYd1ycF`

(This is found in your `.vercel/project.json` file)

## How to Add Secrets to GitHub

1. Go to your GitHub repository: https://github.com/Allweb3Labs/aw3-platform-mock-api

2. Click on "Settings" tab

3. In the left sidebar, click on "Secrets and variables" > "Actions"

4. Click "New repository secret" button

5. Add each secret one by one:
   - Name: `VERCEL_TOKEN`
     Value: [Your Vercel token from step 1 above]
   
   - Name: `VERCEL_ORG_ID`
     Value: `team_IwX8YG1kJSayjFF4VMTQHl0K`
   
   - Name: `VERCEL_PROJECT_ID`
     Value: `prj_jltbOPFNuOMWAgCoWPwSqyYd1ycF`

6. Click "Add secret" after entering each one

## Verify Secrets

After adding all three secrets, you should see them listed on the Actions secrets page:
- VERCEL_TOKEN
- VERCEL_ORG_ID
- VERCEL_PROJECT_ID

## Test the Deployment

Once all secrets are configured:

1. The workflow will automatically run on the next push to main branch

2. Or manually trigger it:
   - Go to "Actions" tab
   - Click "Deploy to Vercel" workflow
   - Click "Run workflow" button
   - Select "main" branch
   - Click "Run workflow"

3. Monitor the deployment:
   - Watch the workflow run in the Actions tab
   - Check for success (green checkmark)
   - Verify deployment at https://swagger-mock-api-five.vercel.app/docs/

## Troubleshooting

### Workflow still failing after adding secrets?

1. **Check token validity:**
   - Ensure the VERCEL_TOKEN is not expired
   - Verify it has the correct permissions

2. **Verify IDs are correct:**
   - Double-check VERCEL_ORG_ID matches your team
   - Confirm VERCEL_PROJECT_ID is for swagger-mock-api project

3. **Check workflow logs:**
   - Go to Actions tab > Failed workflow run
   - Click on the job to see detailed logs
   - Look for specific error messages

### Common errors:

**"Error: Project not found"**
- VERCEL_PROJECT_ID is incorrect
- Token does not have access to the project

**"Error: Forbidden"**
- VERCEL_TOKEN does not have sufficient permissions
- Token belongs to different team/organization

**"Error: Unauthorized"**
- VERCEL_TOKEN is invalid or expired
- Token was not provided correctly

## After Successful Deployment

Once the workflow succeeds:

1. Check Vercel dashboard: https://vercel.com/allweb3labs/swagger-mock-api
2. Verify the API: https://swagger-mock-api-five.vercel.app/api/v1/demo-requests
3. Check documentation: https://swagger-mock-api-five.vercel.app/docs/
4. Test the new GET endpoint for demo requesters

## Workflow Updates

The workflow has been updated to:
- Include VERCEL_ORG_ID and VERCEL_PROJECT_ID as environment variables
- Install npm dependencies before deployment
- Convert swagger.yaml to swagger.json automatically
- Deploy to Vercel with proper authentication

## Security Notes

- Never commit the .vercel directory to Git
- Keep your VERCEL_TOKEN secret and secure
- Rotate tokens periodically for security
- Only grant necessary permissions to tokens
- Use repository secrets, not organization secrets (unless needed)

## Next Steps

1. Add the three required secrets to GitHub repository settings
2. Push the updated workflow file to trigger automatic deployment
3. Monitor the deployment in GitHub Actions
4. Verify the endpoint is working at the production URL
