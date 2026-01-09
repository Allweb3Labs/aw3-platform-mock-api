# Vercel 部署问题排查指南

## 当前问题
Swagger 文档在 https://swagger-mock-api-five.vercel.app/docs/ 未显示更新后的 Request Demo section 顺序。

## 已完成的更改
- ✅ 代码已提交到 GitHub (提交: 456af2a)
- ✅ Request Demo tag 已移到 Creator Profile 之前
- ✅ Request Demo 端点已移到 Creator Profile 端点之前

## 可能的原因和解决方案

### 1. Vercel 自动部署未触发

**检查步骤：**
1. 访问 Vercel Dashboard: https://vercel.com/dashboard
2. 找到项目 `swagger-mock-api` 或相关项目
3. 检查 "Deployments" 页面
4. 查看最新部署的时间戳是否在代码提交之后

**解决方案：**
- 如果最新部署时间早于代码提交，需要手动触发部署
- 在 Vercel Dashboard 中点击 "Redeploy" 或 "Deploy"

### 2. Vercel 项目未连接到正确的 GitHub 仓库

**检查步骤：**
1. 在 Vercel Dashboard 中进入项目设置
2. 检查 "Git" 或 "Settings" > "Git" 部分
3. 确认连接的仓库是：`https://github.com/Allweb3Labs/aw3-platform-mock-api`
4. 确认监听的分支是 `main`

**解决方案：**
- 如果仓库不正确，需要重新连接
- 如果分支不正确，需要更新设置

### 3. 构建缓存问题

**解决方案：**
1. 在 Vercel Dashboard 中进入项目设置
2. 找到 "Settings" > "Git" 或 "Build & Development Settings"
3. 点击 "Clear Build Cache" 或 "Clear Cache"
4. 重新触发部署

### 4. 浏览器缓存问题

**解决方案：**
1. 使用无痕/隐私模式访问：https://swagger-mock-api-five.vercel.app/docs/
2. 或清除浏览器缓存（Ctrl+Shift+Delete）
3. 或硬刷新页面（Ctrl+F5 或 Cmd+Shift+R）

### 5. 部署失败但未通知

**检查步骤：**
1. 在 Vercel Dashboard 中查看最新部署的日志
2. 检查是否有构建错误
3. 检查是否有运行时错误

**常见错误：**
- `swagger.yaml` 语法错误
- 文件路径问题
- 依赖安装失败

## 手动触发部署步骤

### 方法 1: 通过 Vercel Dashboard
1. 登录 https://vercel.com/dashboard
2. 选择项目 `swagger-mock-api`
3. 进入 "Deployments" 页面
4. 点击最新部署右侧的 "..." 菜单
5. 选择 "Redeploy"
6. 或点击 "Deploy" 按钮，选择 "Deploy latest commit"

### 方法 2: 通过 Vercel CLI
```bash
# 安装 Vercel CLI (如果未安装)
npm i -g vercel

# 登录
vercel login

# 在项目目录中部署
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
vercel --prod
```

### 方法 3: 通过 GitHub Webhook
1. 在 GitHub 仓库中创建一个空提交来触发部署：
```bash
git commit --allow-empty -m "Trigger Vercel deployment"
git push origin main
```

## 验证部署是否成功

### 1. 检查 Swagger YAML
访问：https://swagger-mock-api-five.vercel.app/swagger.yaml

搜索以下内容确认顺序：
- `REQUEST DEMO ENDPOINTS` 应该在 `CREATOR PROFILE ENDPOINTS` 之前出现

### 2. 检查 Swagger UI
访问：https://swagger-mock-api-five.vercel.app/docs/

确认：
- "Request Demo" tag 出现在 "Creator Profile" 之前
- `POST /api/v1/demo-requests` 端点显示在 Creator Profile 端点之前

### 3. 检查部署时间戳
在 Vercel Dashboard 中确认最新部署时间在代码提交之后。

## 快速诊断命令

运行以下 PowerShell 命令进行快速诊断：

```powershell
# 检查 GitHub 最新提交
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
git log --oneline -3

# 检查部署的 Swagger 文档
$url = "https://swagger-mock-api-five.vercel.app/swagger.yaml"
$response = Invoke-WebRequest -Uri $url
$content = $response.Content
$demoLine = ($content -split "`n" | Select-String -Pattern "REQUEST DEMO ENDPOINTS").LineNumber
$creatorLine = ($content -split "`n" | Select-String -Pattern "CREATOR PROFILE ENDPOINTS").LineNumber
Write-Host "Request Demo 行号: $demoLine"
Write-Host "Creator Profile 行号: $creatorLine"
if ($demoLine -lt $creatorLine) {
    Write-Host "✓ 顺序正确" -ForegroundColor Green
} else {
    Write-Host "✗ 顺序错误" -ForegroundColor Red
}
```

## 联系支持

如果以上步骤都无法解决问题：
1. 检查 Vercel 项目设置中的构建日志
2. 确认 Vercel 项目是否正确连接到 GitHub
3. 检查是否有环境变量或配置问题
4. 联系 Vercel 支持或查看 Vercel 文档

## 相关链接
- Vercel Dashboard: https://vercel.com/dashboard
- GitHub 仓库: https://github.com/Allweb3Labs/aw3-platform-mock-api
- 部署 URL: https://swagger-mock-api-five.vercel.app/docs/
