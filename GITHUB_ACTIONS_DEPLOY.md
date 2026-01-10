# GitHub Actions 自动部署到 Vercel

## ✅ 已完成的设置

1. ✓ 代码已推送到 GitHub
2. ✓ GitHub Actions workflow 已创建
3. ✓ `vercel.json` 配置已就绪

## 🔑 需要完成的配置

### 步骤 1: 获取 Vercel Token

1. 访问: https://vercel.com/account/tokens
2. 登录你的 Vercel 账户
3. 点击 **"Create Token"**
4. 设置:
   - Name: `github-actions`
   - Scope: **Full Account**
   - Expiration: **No Expiration** 或选择合适的时间
5. 点击 **"Create"**
6. **复制生成的 Token**（只会显示一次！）

### 步骤 2: 获取 Vercel Project 和 Org ID

在你的项目目录中运行：

```bash
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
npx vercel link
```

这将在本地创建 `.vercel` 目录，包含项目配置。

或者：
1. 访问 https://vercel.com/
2. 找到你的项目（或创建新项目）
3. 在项目设置中找到 **Project ID** 和 **Team/Org ID**

### 步骤 3: 在 GitHub 设置 Secrets

1. 访问你的 GitHub 仓库
2. 进入 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **"New repository secret"**
4. 添加以下 secrets:

#### VERCEL_TOKEN
- Name: `VERCEL_TOKEN`
- Value: 从步骤 1 复制的 token

#### VERCEL_ORG_ID (可选，如果使用团队账户)
- Name: `VERCEL_ORG_ID`
- Value: 你的 Vercel 组织 ID

#### VERCEL_PROJECT_ID (可选)
- Name: `VERCEL_PROJECT_ID`
- Value: 你的 Vercel 项目 ID

## 🚀 触发部署

配置完成后，有两种方式触发部署：

### 方式 1: 自动部署（推送代码）
每次推送代码到 `main` 分支时，GitHub Actions 会自动部署：

```bash
git add .
git commit -m "Update API"
git push origin main
```

### 方式 2: 手动触发
1. 访问你的 GitHub 仓库
2. 进入 **Actions** 标签
3. 选择 **"Deploy to Vercel"** workflow
4. 点击 **"Run workflow"**
5. 选择 `main` 分支
6. 点击绿色的 **"Run workflow"** 按钮

## 📊 查看部署状态

### 在 GitHub
1. 进入仓库的 **Actions** 标签
2. 查看最新的 workflow 运行
3. 点击查看详细日志

### 在 Vercel
1. 访问 https://vercel.com/dashboard
2. 找到你的项目
3. 查看 **Deployments** 列表

## 🔧 简化版配置（如果上面太复杂）

如果 GitHub Actions 配置太复杂，可以使用 Vercel 的 GitHub 集成：

1. 访问: https://vercel.com/new
2. 点击 **"Import Git Repository"**
3. 选择你的 GitHub 仓库: `Allweb3Labs/aw3-platform-mock-api`
4. 点击 **"Import"**
5. 配置项目:
   - Framework Preset: **Other**
   - Build Command: 留空
   - Output Directory: 留空
   - Install Command: `npm install`
6. 点击 **"Deploy"**

配置完成后，每次推送到 GitHub 都会自动部署到 Vercel！

## 📝 部署成功后

部署完成后，你将获得：
- 🌐 生产环境 URL: `https://your-project.vercel.app`
- 📊 API 文档: `https://your-project.vercel.app/docs`
- ❤️ 健康检查: `https://your-project.vercel.app/health`
- 📮 Request Demo API: `POST https://your-project.vercel.app/api/v1/demo-requests`

## 🧪 测试 API

```bash
# 健康检查
curl https://your-project.vercel.app/health

# Request Demo
curl -X POST https://your-project.vercel.app/api/v1/demo-requests \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "userType": "creator",
    "socialHandle": "test_handle",
    "socialPlatform": "telegram"
  }'
```

## ❓ 故障排除

### Actions 失败
- 检查 GitHub Secrets 是否正确设置
- 确保 VERCEL_TOKEN 有效且未过期
- 查看 Actions 日志了解详细错误

### 部署失败
- 确保 `package.json` 和 `vercel.json` 配置正确
- 检查 Vercel 项目设置
- 查看 Vercel 部署日志

### 无法访问 API
- 等待几分钟让 DNS 生效
- 清除浏览器缓存
- 检查 Vercel 部署状态是否为 "Ready"
