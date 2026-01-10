# 完全自动化部署脚本
# 使用 Vercel API

$ErrorActionPreference = "Stop"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "🚀 Vercel 完全自动化部署" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 检查 Vercel Token
$vercelToken = $env:VERCEL_TOKEN

if (-not $vercelToken) {
    Write-Host "⚠️  未找到 VERCEL_TOKEN 环境变量`n" -ForegroundColor Yellow
    Write-Host "请按照以下步骤获取 Vercel Token:" -ForegroundColor White
    Write-Host "1. 正在打开 Vercel Token 页面..." -ForegroundColor Gray
    
    # 自动打开浏览器
    Start-Process "https://vercel.com/account/tokens"
    
    Write-Host "`n2. 在浏览器中:" -ForegroundColor White
    Write-Host "   - 登录 Vercel（如果还未登录）" -ForegroundColor Gray
    Write-Host "   - 点击 'Create Token'" -ForegroundColor Gray
    Write-Host "   - Token 名称: auto-deploy" -ForegroundColor Gray
    Write-Host "   - Scope: Full Account" -ForegroundColor Gray
    Write-Host "   - 点击 'Create'" -ForegroundColor Gray
    Write-Host "   - 复制生成的 Token`n" -ForegroundColor Gray
    
    $token = Read-Host "请粘贴 Vercel Token"
    
    if ([string]::IsNullOrWhiteSpace($token)) {
        Write-Host "`n❌ Token 不能为空" -ForegroundColor Red
        exit 1
    }
    
    # 设置环境变量
    $env:VERCEL_TOKEN = $token
    Write-Host "`n✓ Token 已设置`n" -ForegroundColor Green
}

# 确保在项目目录中
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "📂 当前目录: $scriptDir`n" -ForegroundColor Gray

# 检查 Node.js
Write-Host "🔍 检查依赖..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "   ✓ Node.js: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ 请先安装 Node.js" -ForegroundColor Red
    exit 1
}

# 确保依赖已安装
if (-not (Test-Path "node_modules")) {
    Write-Host "`n📦 安装依赖..." -ForegroundColor Yellow
    npm install
}

# 运行部署脚本
Write-Host "`n🚀 开始部署...`n" -ForegroundColor Yellow
node deploy-vercel-api.js

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ 部署完成！" -ForegroundColor Green
} else {
    Write-Host "`n❌ 部署失败" -ForegroundColor Red
    exit 1
}
