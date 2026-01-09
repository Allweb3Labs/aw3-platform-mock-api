# Vercel 部署验证脚本
# 用于测试 Request Demo API 是否已成功部署

param(
    [string]$VercelUrl = ""
)

# 如果没有提供 URL，尝试常见的 Vercel URL
$possibleUrls = @(
    "https://swagger-mock-api-allweb3.vercel.app",
    "https://swagger-mock-api.vercel.app",
    "https://aw3-platform-mock-api.vercel.app"
)

if ($VercelUrl) {
    $testUrls = @($VercelUrl)
} else {
    $testUrls = $possibleUrls
}

Write-Host "=== Vercel 部署验证 ===" -ForegroundColor Cyan
Write-Host ""

$foundDeployment = $false

foreach ($baseUrl in $testUrls) {
    Write-Host "测试部署: $baseUrl" -ForegroundColor Yellow
    
    try {
        # 测试 1: Health Check
        Write-Host "  1. 测试 Health Check..." -NoNewline
        $health = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET -TimeoutSec 10 -ErrorAction Stop
        if ($health.StatusCode -eq 200) {
            Write-Host " ✓ (状态码: $($health.StatusCode))" -ForegroundColor Green
            $healthContent = $health.Content | ConvertFrom-Json
            Write-Host "    响应: $($healthContent | ConvertTo-Json -Compress)" -ForegroundColor Gray
        }
    } catch {
        Write-Host " ✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
        continue
    }
    
    try {
        # 测试 2: Swagger UI
        Write-Host "  2. 测试 Swagger UI..." -NoNewline
        $swagger = Invoke-WebRequest -Uri "$baseUrl/docs" -Method GET -TimeoutSec 10 -ErrorAction Stop
        if ($swagger.StatusCode -eq 200) {
            Write-Host " ✓ (状态码: $($swagger.StatusCode))" -ForegroundColor Green
        }
    } catch {
        Write-Host " ✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    try {
        # 测试 3: Request Demo API - 有效请求
        Write-Host "  3. 测试 Request Demo API (有效请求)..." -NoNewline
        $body = @{
            email = "test@example.com"
            userType = "creator"
            socialHandle = "test_handle"
            socialPlatform = "telegram"
            source = "homepage"
        } | ConvertTo-Json
        
        $headers = @{
            "Content-Type" = "application/json"
        }
        
        $apiResponse = Invoke-WebRequest -Uri "$baseUrl/api/v1/demo-requests" -Method POST -Headers $headers -Body $body -TimeoutSec 10 -ErrorAction Stop
        
        if ($apiResponse.StatusCode -eq 201) {
            Write-Host " ✓ (状态码: $($apiResponse.StatusCode))" -ForegroundColor Green
            $responseData = $apiResponse.Content | ConvertFrom-Json
            Write-Host "    请求ID: $($responseData.data.requestId)" -ForegroundColor Gray
            Write-Host "    邮箱: $($responseData.data.email)" -ForegroundColor Gray
            Write-Host "    用户类型: $($responseData.data.userType)" -ForegroundColor Gray
            Write-Host "    状态: $($responseData.data.status)" -ForegroundColor Gray
        } else {
            Write-Host " ⚠ (状态码: $($apiResponse.StatusCode))" -ForegroundColor Yellow
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 400 -or $statusCode -eq 409 -or $statusCode -eq 429) {
            Write-Host " ⚠ (状态码: $statusCode - 可能是预期的错误)" -ForegroundColor Yellow
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $errorBody = $reader.ReadToEnd()
                $errorData = $errorBody | ConvertFrom-Json
                Write-Host "    错误: $($errorData.error.code) - $($errorData.error.message)" -ForegroundColor Gray
            } catch {}
        } else {
            Write-Host " ✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    try {
        # 测试 4: Request Demo API - 验证错误
        Write-Host "  4. 测试 Request Demo API (验证错误)..." -NoNewline
        $invalidBody = @{
            email = "invalid-email"
            userType = "creator"
            socialHandle = "test"
            socialPlatform = "telegram"
        } | ConvertTo-Json
        
        try {
            $invalidResponse = Invoke-WebRequest -Uri "$baseUrl/api/v1/demo-requests" -Method POST -Headers $headers -Body $invalidBody -TimeoutSec 10 -ErrorAction Stop
            Write-Host " ⚠ (状态码: $($invalidResponse.StatusCode) - 应该返回 400)" -ForegroundColor Yellow
        } catch {
            $statusCode = $_.Exception.Response.StatusCode.value__
            if ($statusCode -eq 400) {
                Write-Host " ✓ (状态码: 400 - 验证错误正确处理)" -ForegroundColor Green
            } else {
                Write-Host " ✗ (状态码: $statusCode)" -ForegroundColor Red
            }
        }
    } catch {
        Write-Host " ✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    try {
        # 测试 5: Swagger YAML 检查 Request Demo section
        Write-Host "  5. 检查 Swagger 文档中的 Request Demo section..." -NoNewline
        $swaggerYaml = Invoke-WebRequest -Uri "$baseUrl/swagger.yaml" -Method GET -TimeoutSec 10 -ErrorAction Stop
        if ($swaggerYaml.Content -match "Request Demo" -or $swaggerYaml.Content -match "demo-requests") {
            Write-Host " ✓ (找到 Request Demo section)" -ForegroundColor Green
        } else {
            Write-Host " ⚠ (未找到 Request Demo section)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host " ✗ 失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
    Write-Host "✓ 部署验证完成: $baseUrl" -ForegroundColor Green
    $foundDeployment = $true
    break
}

if (-not $foundDeployment) {
    Write-Host ""
    Write-Host "✗ 未找到可访问的 Vercel 部署" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "1. Vercel 项目是否已正确配置" -ForegroundColor White
    Write-Host "2. 部署是否已完成" -ForegroundColor White
    Write-Host "3. 提供正确的 Vercel 部署 URL" -ForegroundColor White
    Write-Host ""
    Write-Host "使用方法:" -ForegroundColor Yellow
    Write-Host "  .\test-vercel-deployment.ps1 -VercelUrl 'https://your-vercel-url.vercel.app'" -ForegroundColor White
}
