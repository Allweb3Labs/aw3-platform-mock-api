# Request Demo API 完整部署指南

## ✅ 已完成的工作

1. ✓ Git 仓库已修复
2. ✓ 代码已提交（待推送）
3. ✓ GitHub Actions workflow 已创建
4. ✓ 多种部署脚本已准备就绪
5. ✓ Vercel 配置文件（vercel.json）已就绪

## 🚀 最简单的部署方法（推荐）

### 方法 1: Vercel 网站直接导入（最简单）

1. **推送代码到 GitHub**（当网络恢复时）:
   ```bash
   cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
   git push origin main
   ```

2. **访问 Vercel 网站**:
   - 打开: https://vercel.com/new
   - 使用 GitHub 登录

3. **导入项目**:
   - 点击 "Import Git Repository"
   - 搜索并选择: `Allweb3Labs/aw3-platform-mock-api`
   - 点击 "Import"

4. **配置项目**:
   - Framework Preset: **Other**
   - Root Directory: `.` (默认)
   - Build Command: 留空
   - Output Directory: 留空
   - Install Command: `npm install`

5. **点击 "Deploy"**
   - 等待 1-2 分钟
   - 完成！

**完成后，Vercel 会自动为每次 Git 推送部署新版本！**

---

## 📋 方法 2: 使用 GitHub Actions（自动化）

### 前提条件
- 代码已推送到 GitHub
- 在 GitHub 仓库中配置 Secrets

### 步骤：

1. **获取 Vercel Token**:
   - 访问: https://vercel.com/account/tokens
   - 创建新 Token（名称: github-actions, Scope: Full Account）
   - 复制 Token

2. **在 GitHub 设置 Secret**:
   - 进入仓库: https://github.com/Allweb3Labs/aw3-platform-mock-api
   - Settings → Secrets and variables → Actions
   - 添加 Secret:
     - Name: `VERCEL_TOKEN`
     - Value: 粘贴你的 Token

3. **触发部署**:
   - 推送代码到 main 分支
   - 或在 Actions 标签手动触发

---

## 📋 方法 3: 使用 Vercel CLI（本地部署）

### 步骤：

1. **登录 Vercel**:
   ```bash
   npx vercel login
   ```
   
2. **部署**:
   ```bash
   cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
   npx vercel --prod
   ```

---

## 📋 方法 4: 使用自动化脚本

我们准备了多个自动化脚本：

### deploy-vercel-api.js
使用 Vercel API 部署（需要 token）:
```bash
$env:VERCEL_TOKEN="your_token_here"
node deploy-vercel-api.js
```

### deploy-simple.js
简化版 CLI 部署:
```bash
node deploy-simple.js
```

---

## 🔍 当前状态

### 本地提交
- Commit: `925e01e`
- 消息: "Add GitHub Actions workflow and deployment scripts for Vercel automation"
- 文件: 7 个新文件，977 行代码

### 待推送
代码已提交但未推送到 GitHub（网络问题）

当网络恢复后运行：
```bash
git push origin main
```

---

## 📊 部署后的 API 端点

部署成功后，你将获得：

- **生产 URL**: `https://aw3-platform-mock-api.vercel.app`（或自定义域名）
- **健康检查**: `/health`
- **API 文档**: `/docs`
- **Swagger JSON**: `/swagger.json`
- **Request Demo**: `POST /api/v1/demo-requests`

### 测试命令：

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
    "socialPlatform": "telegram",
    "source": "homepage"
  }'
```

---

## ⚠️ 重要提示

### 数据持久化问题
Vercel 是无服务器环境，`demo-requests.txt` 文件无法持久化。

**解决方案**：
1. 使用 Vercel Postgres
2. 使用 Vercel KV (Redis)
3. 使用外部数据库（MongoDB Atlas, PlanetScale 等）

### 推荐方案：Vercel KV
```javascript
// 安装
npm install @vercel/kv

// 使用
import { kv } from '@vercel/kv';

async function saveDemoRequest(data) {
  await kv.lpush('demo-requests', JSON.stringify(data));
}
```

---

## 📚 相关文档

- `GITHUB_ACTIONS_DEPLOY.md` - GitHub Actions 详细配置
- `DEPLOY_REQUEST_DEMO_API.md` - Request Demo API 部署指南
- `vercel.json` - Vercel 配置文件
- `.github/workflows/deploy-vercel.yml` - GitHub Actions workflow

---

## 🆘 故障排除

### 推送失败
```bash
# 检查网络连接
git remote -v

# 尝试使用 SSH
git remote set-url origin git@github.com:Allweb3Labs/aw3-platform-mock-api.git
git push origin main
```

### Vercel 登录失败
- 清除浏览器缓存
- 尝试使用邮箱登录而不是 GitHub
- 检查网络代理设置

### 部署失败
- 检查 `package.json` 依赖
- 确保 Node.js 版本 >= 18
- 查看 Vercel 部署日志

---

## ✨ 下一步

1. **推送代码到 GitHub**（当网络恢复时）
2. **选择一种部署方法**（推荐方法 1）
3. **测试 API**
4. **添加数据库**（如需持久化）

---

## 🎯 快速开始（推荐流程）

```bash
# 1. 确保代码已提交
git status

# 2. 推送到 GitHub（当网络恢复时）
git push origin main

# 3. 访问 Vercel 网站
# https://vercel.com/new

# 4. 导入项目
# Allweb3Labs/aw3-platform-mock-api

# 5. 点击 Deploy

# 完成！🎉
```

**部署时间**: 约 1-2 分钟  
**零配置**: Vercel 会自动检测 Node.js 项目  
**自动更新**: 每次推送代码都会自动部署

---

祝部署顺利！如有问题，请查看相关文档或 Vercel 支持。
