# Direct Deployment to Vercel Script
# This bypasses GitHub Actions and deploys directly using Vercel CLI

param(
    [Parameter(Mandatory=$false)]
    [string]$VercelToken = $env:VERCEL_TOKEN
)

$ErrorActionPreference = "Stop"

Write-Host "=== Direct Vercel Deployment ===" -ForegroundColor Cyan
Write-Host ""

# Check if Vercel token is provided
if ([string]::IsNullOrWhiteSpace($VercelToken)) {
    Write-Host "Error: VERCEL_TOKEN not provided" -ForegroundColor Red
    Write-Host ""
    Write-Host "Usage:" -ForegroundColor Yellow
    Write-Host "  Option 1: Pass as parameter" -ForegroundColor Gray
    Write-Host "    .\deploy-direct.ps1 -VercelToken YOUR_TOKEN" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Option 2: Set environment variable" -ForegroundColor Gray
    Write-Host "    `$env:VERCEL_TOKEN = 'YOUR_TOKEN'" -ForegroundColor Gray
    Write-Host "    .\deploy-direct.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Get your token at: https://vercel.com/account/tokens" -ForegroundColor Cyan
    exit 1
}

# Step 1: Convert swagger.yaml to swagger.json
Write-Host "Step 1: Converting swagger.yaml to swagger.json..." -ForegroundColor Yellow
try {
    node convert-swagger.js
    if ($LASTEXITCODE -ne 0) {
        throw "Swagger conversion failed"
    }
    Write-Host "  Success: swagger.json created" -ForegroundColor Green
} catch {
    Write-Host "  Error converting swagger: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Check if vercel CLI is installed
Write-Host "`nStep 2: Checking Vercel CLI..." -ForegroundColor Yellow
$vercelInstalled = Get-Command vercel -ErrorAction SilentlyContinue
if (-not $vercelInstalled) {
    Write-Host "  Vercel CLI not found. Installing globally..." -ForegroundColor Yellow
    npm install -g vercel
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  Error installing Vercel CLI" -ForegroundColor Red
        exit 1
    }
    Write-Host "  Success: Vercel CLI installed" -ForegroundColor Green
} else {
    Write-Host "  Success: Vercel CLI already installed" -ForegroundColor Green
}

# Step 3: Deploy to Vercel
Write-Host "`nStep 3: Deploying to Vercel..." -ForegroundColor Yellow
Write-Host "  This may take 1-2 minutes..." -ForegroundColor Gray

try {
    # Deploy to production
    $env:VERCEL_TOKEN = $VercelToken
    $env:VERCEL_ORG_ID = "team_IwX8YG1kJSayjFF4VMTQHl0K"
    $env:VERCEL_PROJECT_ID = "prj_jltbOPFNuOMWAgCoWPwSqyYd1ycF"
    
    $output = vercel --prod --yes --token=$VercelToken 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Success: Deployment complete" -ForegroundColor Green
        Write-Host ""
        Write-Host "=== Deployment Successful ===" -ForegroundColor Green
        Write-Host ""
        Write-Host "Production URL: https://swagger-mock-api-five.vercel.app" -ForegroundColor Cyan
        Write-Host "API Docs: https://swagger-mock-api-five.vercel.app/docs/" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Test the new endpoint:" -ForegroundColor Yellow
        Write-Host "  curl https://swagger-mock-api-five.vercel.app/api/v1/demo-requests" -ForegroundColor Gray
        Write-Host ""
        
        # Test the endpoint
        Write-Host "Testing endpoint..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
        try {
            $response = Invoke-WebRequest -Uri "https://swagger-mock-api-five.vercel.app/api/v1/demo-requests" -Method Get -UseBasicParsing
            Write-Host "  Success: Endpoint is live and responding" -ForegroundColor Green
            Write-Host "  Status Code: $($response.StatusCode)" -ForegroundColor Gray
        } catch {
            Write-Host "  Note: Endpoint may need a few more seconds to become available" -ForegroundColor Yellow
            Write-Host "  Please wait 30-60 seconds and test manually" -ForegroundColor Yellow
        }
    } else {
        throw "Deployment command failed with exit code $LASTEXITCODE"
    }
} catch {
    Write-Host "  Error during deployment: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Yellow
    Write-Host "  1. Verify your VERCEL_TOKEN is valid" -ForegroundColor Gray
    Write-Host "  2. Check token has access to the project" -ForegroundColor Gray
    Write-Host "  3. Ensure you have internet connectivity" -ForegroundColor Gray
    Write-Host "  4. Try logging in: vercel login" -ForegroundColor Gray
    exit 1
}
