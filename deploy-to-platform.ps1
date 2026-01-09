# 统一部署脚本 - 支持多平台部署
# 支持 Vercel、Render、Railway 部署

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("vercel", "render", "railway", "all")]
    [string]$Platform = "all",
    
    [switch]$SkipGit = $false,
    [switch]$SkipPush = $false
)

$ErrorActionPreference = "Stop"

Write-Host "=== 多平台部署脚本 ===" -ForegroundColor Cyan
Write-Host ""

# 项目信息
$projectRoot = "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
$gitRepo = "Allweb3Labs/aw3-platform-mock-api"
$rootDirectory = "BackEnd Endpoint/swagger-mock-api"

# 切换到项目目录
Set-Location $projectRoot

# 步骤 1: Git 操作（可选）
if (-not $SkipGit) {
    Write-Host "1. 检查 Git 状态..." -ForegroundColor Yellow
    $gitStatus = git status --short
    if ($gitStatus) {
        Write-Host "   发现未提交的更改:" -ForegroundColor Yellow
        Write-Host $gitStatus -ForegroundColor Gray
        
        $commit = Read-Host "是否提交这些更改? (y/n)"
        if ($commit -eq "y" -or $commit -eq "Y") {
            $commitMessage = Read-Host "输入提交信息 (或按 Enter 使用默认)"
            if ([string]::IsNullOrWhiteSpace($commitMessage)) {
                $commitMessage = "Update deployment configurations"
            }
            
            git add .
            git commit -m $commitMessage
            Write-Host "   ✓ 更改已提交" -ForegroundColor Green
        }
    } else {
        Write-Host "   ✓ 工作目录干净" -ForegroundColor Green
    }
}

# 步骤 2: 推送到 GitHub（可选）
if (-not $SkipPush) {
    Write-Host "`n2. 推送到 GitHub..." -ForegroundColor Yellow
    try {
        git push origin main
        Write-Host "   ✓ 代码已推送到 GitHub" -ForegroundColor Green
    } catch {
        Write-Host "   ⚠ 推送失败或已是最新: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 步骤 3: 平台特定部署
Write-Host "`n3. 平台部署..." -ForegroundColor Yellow

switch ($Platform.ToLower()) {
    "vercel" {
        Write-Host "`n=== Vercel 部署 ===" -ForegroundColor Cyan
        Write-Host "`nVercel 部署步骤:" -ForegroundColor Yellow
        Write-Host "  1. 访问: https://vercel.com/dashboard" -ForegroundColor White
        Write-Host "  2. 点击 'Add New...' → 'Project'" -ForegroundColor White
        Write-Host "  3. 导入仓库: $gitRepo" -ForegroundColor White
        Write-Host "  4. 配置:" -ForegroundColor White
        Write-Host "     - Framework: Express" -ForegroundColor Gray
        Write-Host "     - Root Directory: $rootDirectory" -ForegroundColor Gray
        Write-Host "     - Build Command: (留空)" -ForegroundColor Gray
        Write-Host "  5. 点击 'Deploy'" -ForegroundColor White
        Write-Host "`n详细指南: VERCEL_FRAMEWORK_SETUP.md" -ForegroundColor Green
    }
    
    "render" {
        Write-Host "`n=== Render 部署 ===" -ForegroundColor Cyan
        Write-Host "`nRender 部署步骤:" -ForegroundColor Yellow
        Write-Host "  1. 访问: https://dashboard.render.com" -ForegroundColor White
        Write-Host "  2. 点击 'New +' → 'Web Service'" -ForegroundColor White
        Write-Host "  3. 连接 GitHub 仓库: $gitRepo" -ForegroundColor White
        Write-Host "  4. 配置:" -ForegroundColor White
        Write-Host "     - Name: swagger-mock-api-v2" -ForegroundColor Gray
        Write-Host "     - Root Directory: $rootDirectory" -ForegroundColor Gray
        Write-Host "     - Environment: Node" -ForegroundColor Gray
        Write-Host "     - Build Command: npm install" -ForegroundColor Gray
        Write-Host "     - Start Command: npm start" -ForegroundColor Gray
        Write-Host "  5. 点击 'Create Web Service'" -ForegroundColor White
        Write-Host "`n配置文件: render.yaml" -ForegroundColor Green
    }
    
    "railway" {
        Write-Host "`n=== Railway 部署 ===" -ForegroundColor Cyan
        Write-Host "`nRailway 部署步骤:" -ForegroundColor Yellow
        Write-Host "  1. 访问: https://railway.app" -ForegroundColor White
        Write-Host "  2. 点击 'New Project'" -ForegroundColor White
        Write-Host "  3. 选择 'Deploy from GitHub repo'" -ForegroundColor White
        Write-Host "  4. 选择仓库: $gitRepo" -ForegroundColor White
        Write-Host "  5. Railway 会自动检测配置" -ForegroundColor White
        Write-Host "  6. 如果需要，设置 Root Directory: $rootDirectory" -ForegroundColor White
        Write-Host "`n配置文件: railway.json" -ForegroundColor Green
    }
    
    "all" {
        Write-Host "`n=== 所有平台部署指南 ===" -ForegroundColor Cyan
        Write-Host ""
        
        Write-Host "Vercel:" -ForegroundColor Yellow
        Write-Host "  - 访问: https://vercel.com/dashboard" -ForegroundColor White
        Write-Host "  - Framework: Express" -ForegroundColor Gray
        Write-Host "  - Root Directory: $rootDirectory" -ForegroundColor Gray
        Write-Host "  - 详细指南: VERCEL_FRAMEWORK_SETUP.md" -ForegroundColor Gray
        Write-Host ""
        
        Write-Host "Render:" -ForegroundColor Yellow
        Write-Host "  - 访问: https://dashboard.render.com" -ForegroundColor White
        Write-Host "  - Root Directory: $rootDirectory" -ForegroundColor Gray
        Write-Host "  - 配置文件: render.yaml" -ForegroundColor Gray
        Write-Host ""
        
        Write-Host "Railway:" -ForegroundColor Yellow
        Write-Host "  - 访问: https://railway.app" -ForegroundColor White
        Write-Host "  - 自动检测配置" -ForegroundColor Gray
        Write-Host "  - 配置文件: railway.json" -ForegroundColor Gray
        Write-Host ""
        
        Write-Host "详细对比: DEPLOYMENT_PLATFORM_COMPARISON.md" -ForegroundColor Green
    }
}

Write-Host "`n=== 部署准备完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "下一步:" -ForegroundColor Cyan
Write-Host "  1. 按照上述步骤在对应平台创建项目" -ForegroundColor White
Write-Host "  2. 等待部署完成（1-5 分钟）" -ForegroundColor White
Write-Host "  3. 验证部署:" -ForegroundColor White
Write-Host "     - 健康检查: /health" -ForegroundColor Gray
Write-Host "     - Swagger UI: /docs" -ForegroundColor Gray
Write-Host "     - 确认 Request Demo 在 Creator Profile 之前" -ForegroundColor Gray
Write-Host ""
