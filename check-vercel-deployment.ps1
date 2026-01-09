# 检查 Vercel 部署状态的脚本

param(
    [string]$VercelUrl = "https://swagger-mock-api-five.vercel.app"
)

Write-Host "=== Vercel 部署状态检查 ===" -ForegroundColor Cyan
Write-Host ""

# 检查 Swagger YAML
Write-Host "1. 检查 Swagger YAML 文件..." -ForegroundColor Yellow
$yamlUrl = "$VercelUrl/swagger.yaml"
try {
    $yamlResponse = Invoke-RestMethod -Uri $yamlUrl -Method Get -TimeoutSec 10 -ErrorAction Stop
    Write-Host "   ✓ Swagger YAML 可访问" -ForegroundColor Green
    
    # 检查顺序
    $yamlLines = $yamlResponse -split "`n"
    $demoIndex = -1
    $creatorIndex = -1
    
    for ($i = 0; $i -lt $yamlLines.Length; $i++) {
        if ($yamlLines[$i] -match "REQUEST DEMO ENDPOINTS") {
            $demoIndex = $i
        }
        if ($yamlLines[$i] -match "CREATOR PROFILE ENDPOINTS") {
            $creatorIndex = $i
        }
    }
    
    if ($demoIndex -ge 0 -and $creatorIndex -ge 0) {
        if ($demoIndex -lt $creatorIndex) {
            Write-Host "   ✓ Request Demo 在 Creator Profile 之前 (行 $($demoIndex + 1) vs $($creatorIndex + 1))" -ForegroundColor Green
        } else {
            Write-Host "   ✗ Request Demo 不在 Creator Profile 之前 (行 $($demoIndex + 1) vs $($creatorIndex + 1))" -ForegroundColor Red
            Write-Host "   ⚠ 需要重新部署" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ⚠ 无法找到端点标记" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ✗ 无法访问 Swagger YAML: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 检查健康端点
Write-Host "2. 检查健康端点..." -ForegroundColor Yellow
$healthUrl = "$VercelUrl/health"
try {
    $healthResponse = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 10 -ErrorAction Stop
    Write-Host "   ✓ 服务器运行正常" -ForegroundColor Green
} catch {
    Write-Host "   ✗ 健康检查失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 检查 Swagger UI
Write-Host "3. 检查 Swagger UI..." -ForegroundColor Yellow
$docsUrl = "$VercelUrl/docs"
try {
    $docsResponse = Invoke-WebRequest -Uri $docsUrl -Method Get -TimeoutSec 10 -ErrorAction Stop
    if ($docsResponse.StatusCode -eq 200) {
        Write-Host "   ✓ Swagger UI 可访问 (状态码: $($docsResponse.StatusCode))" -ForegroundColor Green
        Write-Host "   ℹ 请使用无痕模式访问以排除缓存问题" -ForegroundColor Cyan
    }
} catch {
    Write-Host "   ✗ 无法访问 Swagger UI: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 建议操作 ===" -ForegroundColor Cyan
Write-Host "1. 访问 Vercel Dashboard: https://vercel.com/dashboard" -ForegroundColor White
Write-Host "2. 找到项目并检查最新部署时间" -ForegroundColor White
Write-Host "3. 如果部署时间早于代码提交，手动触发重新部署" -ForegroundColor White
Write-Host "4. 清除 Vercel 构建缓存" -ForegroundColor White
Write-Host "5. 使用无痕模式访问: $docsUrl" -ForegroundColor White
