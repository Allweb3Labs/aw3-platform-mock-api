# Request Demo API 一键部署脚本
# 使用方法: .\deploy-request-demo.ps1

$ErrorActionPreference = "Stop"
$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Request Demo API Vercel 部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否安装了 Node.js
Write-Host "[1/5] 检查 Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "   ✓ Node.js 版本: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Node.js 未安装，请先安装 Node.js" -ForegroundColor Red
    exit 1
}

# 检查是否安装了 npm
Write-Host "[2/5] 检查 npm..." -ForegroundColor Yellow
try {
    $npmVersion = npm --version
    Write-Host "   ✓ npm 版本: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ npm 未安装" -ForegroundColor Red
    exit 1
}

# 安装依赖
Write-Host "[3/5] 安装项目依赖..." -ForegroundColor Yellow
Set-Location $ProjectDir
npm install
Write-Host "   ✓ 依赖安装完成" -ForegroundColor Green

# 检查 Vercel CLI
Write-Host "[4/5] 检查 Vercel CLI..." -ForegroundColor Yellow
$vercelInstalled = $false
try {
    $vercelVersion = vercel --version 2>$null
    if ($vercelVersion) {
        $vercelInstalled = $true
        Write-Host "   ✓ Vercel CLI 版本: $vercelVersion" -ForegroundColor Green
    }
} catch {}

if (-not $vercelInstalled) {
    Write-Host "   正在安装 Vercel CLI..." -ForegroundColor Yellow
    npm install -g vercel
    Write-Host "   ✓ Vercel CLI 安装完成" -ForegroundColor Green
}

# 部署到 Vercel
Write-Host "[5/5] 部署到 Vercel..." -ForegroundColor Yellow
Write-Host ""
Write-Host "如果是首次部署，Vercel 会要求你登录并配置项目。" -ForegroundColor Cyan
Write-Host "请按照提示操作。" -ForegroundColor Cyan
Write-Host ""

# 检查是否已登录
$loginCheck = vercel whoami 2>&1
if ($loginCheck -match "Error") {
    Write-Host "请先登录 Vercel..." -ForegroundColor Yellow
    vercel login
}

# 部署
Write-Host ""
Write-Host "开始部署到生产环境..." -ForegroundColor Yellow
vercel --prod

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "测试 Request Demo API:" -ForegroundColor Cyan
Write-Host 'curl -X POST https://your-project.vercel.app/api/v1/demo-requests \' -ForegroundColor Gray
Write-Host '  -H "Content-Type: application/json" \' -ForegroundColor Gray
Write-Host '  -d ''{"email":"test@example.com","userType":"creator","socialHandle":"test","socialPlatform":"telegram"}''' -ForegroundColor Gray
Write-Host ""
