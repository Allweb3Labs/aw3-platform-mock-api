# Automated Deployment Script for AW3 Platform Mock API
# This script will push changes to GitHub and trigger Render deployment

Write-Host "=== AW3 Platform Mock API - Automated Deployment ===" -ForegroundColor Cyan
Write-Host ""

# Configuration
$repoPath = "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
$gitRemote = "https://github.com/Allweb3Labs/aw3-platform-mock-api.git"
$renderUrl = "https://aw3-platform-mock-api.onrender.com/docs"

# Change to repository directory
Set-Location $repoPath
Write-Host "Working directory: $repoPath" -ForegroundColor Yellow

# Check git status
Write-Host "`nChecking git status..." -ForegroundColor Yellow
git status

# Configure git for large files and network issues
Write-Host "`nConfiguring git for optimal performance..." -ForegroundColor Yellow
git config http.postBuffer 524288000
git config http.lowSpeedLimit 0
git config http.lowSpeedTime 999999

# Try multiple push methods
$pushSuccess = $false
$maxRetries = 3
$retryCount = 0

while (-not $pushSuccess -and $retryCount -lt $maxRetries) {
    $retryCount++
    Write-Host "`nAttempt $retryCount of $maxRetries..." -ForegroundColor Yellow
    
    try {
        # Method 1: Standard HTTPS push
        Write-Host "Attempting to push via HTTPS..." -ForegroundColor Cyan
        $result = git push origin main 2>&1
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "SUCCESS: Changes pushed to GitHub!" -ForegroundColor Green
            $pushSuccess = $true
        } else {
            Write-Host "FAILED: $result" -ForegroundColor Red
            
            # Wait before retry
            if ($retryCount -lt $maxRetries) {
                Write-Host "Waiting 5 seconds before retry..." -ForegroundColor Yellow
                Start-Sleep -Seconds 5
            }
        }
    }
    catch {
        Write-Host "ERROR: $_" -ForegroundColor Red
    }
}

if ($pushSuccess) {
    Write-Host "`n=== DEPLOYMENT INITIATED ===" -ForegroundColor Green
    Write-Host "Changes have been pushed to GitHub successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Render will automatically detect the changes and start deployment." -ForegroundColor Cyan
    Write-Host "This typically takes 3-5 minutes for free tier services." -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Deployment Links:" -ForegroundColor Yellow
    Write-Host "  - GitHub Commits: https://github.com/Allweb3Labs/aw3-platform-mock-api/commits/main" -ForegroundColor White
    Write-Host "  - Render Dashboard: https://dashboard.render.com/web/srv-d4mi7kili9vc73ercdn0" -ForegroundColor White
    Write-Host "  - API Documentation: $renderUrl" -ForegroundColor White
    Write-Host ""
    Write-Host "Waiting for Render deployment to complete..." -ForegroundColor Yellow
    Write-Host "Checking deployment status in 30 seconds..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
    # Test the API
    Write-Host "`nTesting API endpoint..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "https://aw3-platform-mock-api.onrender.com/health" -Method Get -TimeoutSec 10
        Write-Host "API is responding: $response" -ForegroundColor Green
    }
    catch {
        Write-Host "API is still deploying or starting up..." -ForegroundColor Yellow
        Write-Host "Please wait a few more minutes and check: $renderUrl" -ForegroundColor Cyan
    }
    
    Write-Host "`n=== DEPLOYMENT COMPLETE ===" -ForegroundColor Green
    Write-Host "Visit the Swagger UI to verify Project Portal endpoints are available:" -ForegroundColor Cyan
    Write-Host $renderUrl -ForegroundColor White
    
} else {
    Write-Host "`n=== MANUAL ACTION REQUIRED ===" -ForegroundColor Red
    Write-Host "Automated push failed after $maxRetries attempts." -ForegroundColor Red
    Write-Host ""
    Write-Host "Please use one of these manual methods:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "METHOD 1: GitHub Desktop (Recommended)" -ForegroundColor Cyan
    Write-Host "  1. Open GitHub Desktop" -ForegroundColor White
    Write-Host "  2. File > Add Local Repository" -ForegroundColor White
    Write-Host "  3. Choose: $repoPath" -ForegroundColor White
    Write-Host "  4. Click 'Push origin' button" -ForegroundColor White
    Write-Host ""
    Write-Host "METHOD 2: Visual Studio Code" -ForegroundColor Cyan
    Write-Host "  1. Open folder: $repoPath" -ForegroundColor White
    Write-Host "  2. Source Control tab (Ctrl+Shift+G)" -ForegroundColor White
    Write-Host "  3. Click '...' > Push" -ForegroundColor White
    Write-Host ""
    Write-Host "METHOD 3: Configure SSH" -ForegroundColor Cyan
    Write-Host "  Run: git remote set-url origin git@github.com:Allweb3Labs/aw3-platform-mock-api.git" -ForegroundColor White
    Write-Host "  Then: git push origin main" -ForegroundColor White
    Write-Host ""
}

Write-Host "`nPress any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

