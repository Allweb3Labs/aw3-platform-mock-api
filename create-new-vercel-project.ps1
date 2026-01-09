# 创建新 Vercel 项目的辅助脚本
# 由于 API 限制，此脚本主要用于验证和准备

param(
    [string]$NewProjectName = "swagger-mock-api-v2"
)

Write-Host "=== 创建新 Vercel 项目准备 ===" -ForegroundColor Cyan
Write-Host ""

# 检查当前项目信息
Write-Host "1. 检查当前项目配置..." -ForegroundColor Yellow
$gitRemote = git remote get-url origin
Write-Host "   GitHub 仓库: $gitRemote" -ForegroundColor Gray

$currentBranch = git branch --show-current
Write-Host "   当前分支: $currentBranch" -ForegroundColor Gray

$latestCommit = git log -1 --oneline
Write-Host "   最新提交: $latestCommit" -ForegroundColor Gray

# 检查 swagger.yaml
Write-Host "`n2. 验证代码包含 Request Demo..." -ForegroundColor Yellow
$yamlContent = Get-Content "swagger.yaml" -Raw
$hasRequestDemo = $yamlContent -match "REQUEST DEMO ENDPOINTS"
$hasCreatorProfile = $yamlContent -match "CREATOR PROFILE ENDPOINTS"

if ($hasRequestDemo -and $hasCreatorProfile) {
    $demoLine = ($yamlContent -split "`n" | Select-String -Pattern "REQUEST DEMO ENDPOINTS").LineNumber
    $creatorLine = ($yamlContent -split "`n" | Select-String -Pattern "CREATOR PROFILE ENDPOINTS").LineNumber
    
    if ($demoLine[0] -lt $creatorLine[0]) {
        Write-Host "   ✓ Request Demo 在 Creator Profile 之前" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 顺序错误" -ForegroundColor Red
    }
} else {
    Write-Host "   ⚠ 未找到 Request Demo section" -ForegroundColor Yellow
}

# 检查 vercel.json
Write-Host "`n3. 检查 Vercel 配置..." -ForegroundColor Yellow
if (Test-Path "vercel.json") {
    Write-Host "   ✓ vercel.json 存在" -ForegroundColor Green
    $vercelConfig = Get-Content "vercel.json" | ConvertFrom-Json
    Write-Host "   - 版本: $($vercelConfig.version)" -ForegroundColor Gray
} else {
    Write-Host "   ⚠ vercel.json 不存在" -ForegroundColor Yellow
}

# 检查 package.json
Write-Host "`n4. 检查项目依赖..." -ForegroundColor Yellow
if (Test-Path "package.json") {
    $package = Get-Content "package.json" | ConvertFrom-Json
    Write-Host "   ✓ package.json 存在" -ForegroundColor Green
    Write-Host "   - 项目名称: $($package.name)" -ForegroundColor Gray
    Write-Host "   - Node 版本要求: $($package.engines.node)" -ForegroundColor Gray
} else {
    Write-Host "   ✗ package.json 不存在" -ForegroundColor Red
}

Write-Host "`n=== 准备完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "📋 下一步操作:" -ForegroundColor Cyan
Write-Host ""
Write-Host "方法 1: 通过 Vercel Dashboard（推荐）" -ForegroundColor Yellow
Write-Host "  1. 访问: https://vercel.com/dashboard" -ForegroundColor White
Write-Host "  2. 点击 'Add New...' → 'Project'" -ForegroundColor White
Write-Host "  3. 导入仓库: Allweb3Labs/aw3-platform-mock-api" -ForegroundColor White
Write-Host "  4. 项目名称: $NewProjectName" -ForegroundColor White
Write-Host "  5. 框架: Other 或 Node.js" -ForegroundColor White
Write-Host "  6. 根目录: BackEnd Endpoint/swagger-mock-api (如果代码在子目录)" -ForegroundColor White
Write-Host "  7. 点击 'Deploy'" -ForegroundColor White
Write-Host ""
Write-Host "方法 2: 使用 Vercel CLI" -ForegroundColor Yellow
Write-Host "  npm i -g vercel" -ForegroundColor White
Write-Host "  vercel login" -ForegroundColor White
Write-Host "  cd '$(Get-Location)'" -ForegroundColor White
Write-Host "  vercel --prod" -ForegroundColor White
Write-Host ""
Write-Host "详细指南: CREATE_NEW_VERCEL_PROJECT.md" -ForegroundColor Green
