# 完整的自动化部署脚本
# 包括 Git 提交、推送和 Vercel 部署触发

param(
    [Parameter(Mandatory=$true)]
    [string]$VercelToken,
    
    [string]$ProjectName = "swagger-mock-api",
    [string]$TeamName = "allweb3",
    [switch]$SkipGit = $false,
    [switch]$SkipPush = $false
)

$ErrorActionPreference = "Stop"

Write-Host "=== 自动化部署流程 ===" -ForegroundColor Cyan
Write-Host ""

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
                $commitMessage = "Auto commit before Vercel deployment"
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

# 步骤 3: 触发 Vercel 部署
Write-Host "`n3. 触发 Vercel 部署..." -ForegroundColor Yellow

$apiBase = "https://api.vercel.com"
$headers = @{
    "Authorization" = "Bearer $VercelToken"
    "Content-Type" = "application/json"
}

try {
    # 获取项目信息
    $projectUrl = "$apiBase/v9/projects/$ProjectName"
    if ($TeamName) {
        $projectUrl += "?teamId=$TeamName"
    }
    
    $project = Invoke-RestMethod -Uri $projectUrl -Method Get -Headers $headers
    Write-Host "   ✓ 项目找到: $($project.name)" -ForegroundColor Green
    
    # 创建新部署
    $deployUrl = "$apiBase/v13/deployments"
    $deployBody = @{
        name = $ProjectName
        gitSource = @{
            type = "github"
            repo = $project.link.repo
            ref = "main"
        }
        target = "production"
    } | ConvertTo-Json -Depth 10
    
    $deployment = Invoke-RestMethod -Uri $deployUrl -Method Post -Headers $headers -Body $deployBody
    Write-Host "   ✓ 部署已触发" -ForegroundColor Green
    Write-Host "   - 部署 ID: $($deployment.id)" -ForegroundColor Gray
    Write-Host "   - 状态: $($deployment.state)" -ForegroundColor Gray
    Write-Host "   - URL: $($deployment.url)" -ForegroundColor Gray
    
    Write-Host "`n=== 部署触发成功 ===" -ForegroundColor Green
    Write-Host "部署详情: https://vercel.com/$TeamName/$ProjectName/$($deployment.id)" -ForegroundColor Cyan
    Write-Host "`n您可以在 Vercel Dashboard 中查看部署进度" -ForegroundColor Yellow
    
} catch {
    Write-Host "`n✗ 部署触发失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应详情: $responseBody" -ForegroundColor Red
    }
    exit 1
}
