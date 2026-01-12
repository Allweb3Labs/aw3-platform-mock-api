# Trigger Vercel Deployment via API
# This bypasses GitHub Actions completely

param(
    [Parameter(Mandatory=$true)]
    [string]$VercelToken
)

$ErrorActionPreference = "Stop"

Write-Host "=== Triggering Vercel Deployment via API ===" -ForegroundColor Cyan
Write-Host ""

# Configuration
$orgId = "team_IwX8YG1kJSayjFF4VMTQHl0K"
$projectId = "prj_jltbOPFNuOMWAgCoWPwSqyYd1ycF"
$projectName = "swagger-mock-api"

# Headers
$headers = @{
    "Authorization" = "Bearer $VercelToken"
    "Content-Type" = "application/json"
}

# Step 1: Get latest commit from GitHub
Write-Host "Step 1: Getting latest commit info..." -ForegroundColor Yellow
try {
    $gitLog = git log -1 --format="%H %s" 2>&1
    $commitHash = ($gitLog -split " ")[0]
    Write-Host "  Latest commit: $commitHash" -ForegroundColor Gray
} catch {
    Write-Host "  Could not get git info: $_" -ForegroundColor Yellow
    $commitHash = "main"
}

# Step 2: Create deployment
Write-Host "`nStep 2: Creating deployment..." -ForegroundColor Yellow

$deploymentBody = @{
    name = $projectName
    project = $projectId
    target = "production"
    gitSource = @{
        type = "github"
        repoId = "aw3-platform-mock-api"
        ref = "main"
    }
} | ConvertTo-Json -Depth 10

try {
    $deployUrl = "https://api.vercel.com/v13/deployments?teamId=$orgId"
    $response = Invoke-RestMethod -Uri $deployUrl -Method Post -Headers $headers -Body $deploymentBody
    
    Write-Host "  Deployment created!" -ForegroundColor Green
    Write-Host "  Deployment ID: $($response.id)" -ForegroundColor Gray
    Write-Host "  URL: $($response.url)" -ForegroundColor Cyan
    Write-Host "  Status: $($response.readyState)" -ForegroundColor Gray
    
    # Step 3: Wait for deployment
    Write-Host "`nStep 3: Waiting for deployment to complete..." -ForegroundColor Yellow
    
    $maxWait = 180  # 3 minutes
    $waited = 0
    $deploymentId = $response.id
    
    while ($waited -lt $maxWait) {
        Start-Sleep -Seconds 10
        $waited += 10
        
        $statusUrl = "https://api.vercel.com/v13/deployments/$deploymentId`?teamId=$orgId"
        $status = Invoke-RestMethod -Uri $statusUrl -Method Get -Headers $headers
        
        Write-Host "  [$waited s] Status: $($status.readyState)" -ForegroundColor Gray
        
        if ($status.readyState -eq "READY") {
            Write-Host "`n=== Deployment Successful! ===" -ForegroundColor Green
            Write-Host "Production URL: https://swagger-mock-api-five.vercel.app" -ForegroundColor Cyan
            Write-Host "Deployment URL: https://$($status.url)" -ForegroundColor Cyan
            
            # Test the endpoint
            Write-Host "`nStep 4: Testing endpoint..." -ForegroundColor Yellow
            Start-Sleep -Seconds 5
            
            try {
                $testResponse = Invoke-WebRequest -Uri "https://swagger-mock-api-five.vercel.app/api/v1/demo-requests" -Method Get -UseBasicParsing
                Write-Host "  Endpoint is LIVE! Status: $($testResponse.StatusCode)" -ForegroundColor Green
            } catch {
                Write-Host "  Note: Endpoint may take a few more seconds to propagate" -ForegroundColor Yellow
            }
            
            exit 0
        }
        elseif ($status.readyState -eq "ERROR" -or $status.readyState -eq "CANCELED") {
            Write-Host "`n=== Deployment Failed ===" -ForegroundColor Red
            Write-Host "Status: $($status.readyState)" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host "`nTimeout waiting for deployment" -ForegroundColor Yellow
    Write-Host "Check Vercel dashboard: https://vercel.com/allweb3labs/swagger-mock-api" -ForegroundColor Cyan
    
} catch {
    Write-Host "  Error creating deployment: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Response: $responseBody" -ForegroundColor Red
    }
    
    exit 1
}
