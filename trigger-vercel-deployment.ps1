# Vercel 自动部署触发脚本
# 使用 Vercel API 触发重新部署

param(
    [Parameter(Mandatory=$true)]
    [string]$VercelToken,
    
    [string]$ProjectName = "swagger-mock-api",
    [string]$TeamName = "allweb3"
)

Write-Host "=== Vercel 自动部署触发 ===" -ForegroundColor Cyan
Write-Host ""

# Vercel API 端点
$apiBase = "https://api.vercel.com"
$headers = @{
    "Authorization" = "Bearer $VercelToken"
    "Content-Type" = "application/json"
}

try {
    # 步骤 1: 获取项目信息
    Write-Host "1. 获取项目信息..." -ForegroundColor Yellow
    $projectUrl = "$apiBase/v9/projects/$ProjectName"
    if ($TeamName) {
        $projectUrl += "?teamId=$TeamName"
    }
    
    $project = Invoke-RestMethod -Uri $projectUrl -Method Get -Headers $headers -ErrorAction Stop
    Write-Host "   ✓ 项目找到: $($project.name)" -ForegroundColor Green
    Write-Host "   - 项目 ID: $($project.id)" -ForegroundColor Gray
    
    # 步骤 2: 获取最新部署
    Write-Host "`n2. 获取最新部署信息..." -ForegroundColor Yellow
    $deploymentsUrl = "$apiBase/v6/deployments?projectId=$($project.id)&limit=1"
    if ($TeamName) {
        $deploymentsUrl += "&teamId=$TeamName"
    }
    
    $deployments = Invoke-RestMethod -Uri $deploymentsUrl -Method Get -Headers $headers -ErrorAction Stop
    $latestDeployment = $deployments.deployments[0]
    
    Write-Host "   ✓ 最新部署找到" -ForegroundColor Green
    Write-Host "   - 部署 ID: $($latestDeployment.uid)" -ForegroundColor Gray
    Write-Host "   - 状态: $($latestDeployment.state)" -ForegroundColor Gray
    Write-Host "   - 创建时间: $($latestDeployment.createdAt)" -ForegroundColor Gray
    
    # 步骤 3: 触发重新部署
    Write-Host "`n3. 触发重新部署..." -ForegroundColor Yellow
    $redeployUrl = "$apiBase/v13/deployments/$($latestDeployment.uid)/cancel"
    
    # 首先取消当前部署（如果需要）
    # 然后创建新部署
    $newDeployUrl = "$apiBase/v13/deployments"
    $deployBody = @{
        name = $ProjectName
        project = $project.id
        target = "production"
    } | ConvertTo-Json
    
    $newDeployment = Invoke-RestMethod -Uri $newDeployUrl -Method Post -Headers $headers -Body $deployBody -ErrorAction Stop
    Write-Host "   ✓ 新部署已创建" -ForegroundColor Green
    Write-Host "   - 部署 ID: $($newDeployment.id)" -ForegroundColor Gray
    Write-Host "   - 状态: $($newDeployment.state)" -ForegroundColor Gray
    Write-Host "   - URL: $($newDeployment.url)" -ForegroundColor Gray
    
    Write-Host "`n=== 部署触发成功 ===" -ForegroundColor Green
    Write-Host "部署 URL: https://vercel.com/$TeamName/$ProjectName/$($newDeployment.id)" -ForegroundColor Cyan
    Write-Host "`n您可以在 Vercel Dashboard 中查看部署进度" -ForegroundColor Yellow
    
} catch {
    Write-Host "`n✗ 错误: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应: $responseBody" -ForegroundColor Red
    }
    Write-Host "`n请检查:" -ForegroundColor Yellow
    Write-Host "  1. Vercel Token 是否正确" -ForegroundColor White
    Write-Host "  2. 项目名称是否正确: $ProjectName" -ForegroundColor White
    Write-Host "  3. Team 名称是否正确: $TeamName" -ForegroundColor White
    exit 1
}
