# Request Demo API 部署到 Vercel 指南

## 📋 API 端点信息

Request Demo API 已经在 `server.js` 中实现：

- **端点**: `POST /api/v1/demo-requests`
- **功能**: 接收用户的 demo 请求
- **数据存储**: 本地文件 `demo-requests.txt`

## 🚀 快速部署步骤

### 方法 1: 通过 Vercel 网站部署（推荐）

1. **访问 Vercel 网站**
   - 打开浏览器，访问 https://vercel.com
   - 登录你的账户

2. **导入项目**
   - 点击 "Add New" → "Project"
   - 选择 "Import Git Repository"
   - 连接 GitHub 账户（如果还没连接）
   - 选择仓库: `Allweb3Labs/aw3-platform-mock-api`

3. **配置项目**
   - Framework Preset: 选择 "Other"
   - Root Directory: 保持默认
   - Build Command: 留空（无需构建）
   - Output Directory: 留空
   - Install Command: `npm install`

4. **环境变量（可选）**
   - NODE_ENV: production

5. **点击 "Deploy"**
   - 等待部署完成（约1-2分钟）

### 方法 2: 通过 Vercel CLI 部署

```powershell
# 1. 安装 Vercel CLI（如果未安装）
npm install -g vercel

# 2. 登录 Vercel
vercel login

# 3. 进入项目目录
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"

# 4. 部署到生产环境
vercel --prod
```

### 方法 3: 修复 Git 并推送

由于当前 Git 仓库有损坏，请按以下步骤操作：

```powershell
# 1. 备份当前目录
$source = "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
$backup = "A:\Web3\Allweb3 PM\Back-End\swagger-mock-api-backup"
Copy-Item -Path $source -Destination $backup -Recurse -Exclude ".git"

# 2. 删除损坏的 .git 目录
Remove-Item -Path "$source\.git" -Recurse -Force

# 3. 重新克隆仓库
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint"
Remove-Item -Path "swagger-mock-api" -Recurse -Force
git clone https://github.com/Allweb3Labs/aw3-platform-mock-api.git swagger-mock-api

# 4. 复制备份文件回来
Copy-Item -Path "$backup\*" -Destination "$source" -Recurse -Force

# 5. 提交并推送
cd swagger-mock-api
git add .
git commit -m "Update Request Demo API for Vercel deployment"
git push origin main
```

## 📝 Vercel 配置文件

项目已包含 `vercel.json` 配置：

```json
{
  "version": 2,
  "builds": [
    {
      "src": "server.js",
      "use": "@vercel/node"
    }
  ],
  "routes": [
    {
      "src": "/(.*)",
      "dest": "server.js"
    }
  ],
  "env": {
    "NODE_ENV": "production"
  }
}
```

## 🧪 测试 API

部署成功后，测试 Request Demo API：

```bash
# 测试健康检查
curl https://your-project.vercel.app/health

# 测试 Request Demo 端点
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

## ⚠️ 注意事项

### Vercel 无服务器环境限制

由于 Vercel 使用无服务器架构，文件系统是只读的。这意味着：

1. **数据存储问题**: `demo-requests.txt` 文件在 Vercel 上无法持久化写入
2. **解决方案选项**:
   - 使用数据库（如 Vercel Postgres、PlanetScale）
   - 使用外部存储（如 Redis、MongoDB Atlas）
   - 使用 Vercel KV 或 Blob Storage

### 推荐数据库方案

如需持久化存储 demo 请求，建议添加数据库支持：

```javascript
// 使用 Vercel Postgres 示例
import { sql } from '@vercel/postgres';

export async function saveDemoRequest(data) {
  await sql`
    INSERT INTO demo_requests (request_id, email, user_type, social_handle, social_platform, source, created_at)
    VALUES (${data.requestId}, ${data.email}, ${data.userType}, ${data.socialHandle}, ${data.socialPlatform}, ${data.source}, NOW())
  `;
}
```

## 📊 部署成功后的 URL

部署完成后，你将获得以下 URL：

- **API 根**: `https://your-project.vercel.app/`
- **Swagger UI**: `https://your-project.vercel.app/docs`
- **健康检查**: `https://your-project.vercel.app/health`
- **Request Demo API**: `https://your-project.vercel.app/api/v1/demo-requests`

## 🔗 相关资源

- [Vercel 文档](https://vercel.com/docs)
- [Vercel Node.js Runtime](https://vercel.com/docs/runtimes/node-js)
- [项目 GitHub 仓库](https://github.com/Allweb3Labs/aw3-platform-mock-api)
