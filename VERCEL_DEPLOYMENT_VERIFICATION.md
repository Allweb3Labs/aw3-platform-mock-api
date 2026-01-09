# Vercel 部署验证指南

## 当前状态

✅ **代码已推送到 GitHub**
- 提交 ID: `6e250ae`
- 提交信息: "Add Request Demo API endpoint with validation, rate limiting, and txt storage"
- 仓库: https://github.com/Allweb3Labs/aw3-platform-mock-api
- 分支: `main`

## 验证步骤

### 1. 检查 Vercel 控制台

访问 Vercel 项目控制台：
- **URL**: https://vercel.com/allweb3/swagger-mock-api
- 或登录 Vercel Dashboard 查找项目

在控制台中检查：
- ✅ 最新的部署状态应为 "Ready"
- ✅ 部署时间应显示为最近的提交时间
- ✅ 构建日志中应显示成功构建

### 2. 获取部署 URL

在 Vercel 控制台的部署详情中：
- 点击 "Visit" 按钮获取实际部署 URL
- 通常格式为：`https://[project-name].vercel.app`
- 或预览 URL：`https://[project-name]-[hash].vercel.app`

### 3. 使用验证脚本测试

运行提供的 PowerShell 验证脚本：

```powershell
# 使用默认 URL 列表测试
.\test-vercel-deployment.ps1

# 或指定确切的 Vercel URL
.\test-vercel-deployment.ps1 -VercelUrl "https://your-vercel-url.vercel.app"
```

### 4. 手动测试 API 端点

#### 4.1 健康检查
```bash
curl https://your-vercel-url.vercel.app/health
```

预期响应：
```json
{
  "status": "ok",
  "timestamp": "2026-01-08T..."
}
```

#### 4.2 Swagger UI
访问：`https://your-vercel-url.vercel.app/docs`

检查是否包含：
- ✅ "Request Demo" tag
- ✅ `POST /api/v1/demo-requests` 端点
- ✅ DemoRequestPayload schema
- ✅ DemoRequestResponse schema

#### 4.3 测试 Request Demo API - 有效请求
```bash
curl -X POST https://your-vercel-url.vercel.app/api/v1/demo-requests \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "userType": "creator",
    "socialHandle": "test_handle",
    "socialPlatform": "telegram",
    "source": "homepage"
  }'
```

预期响应（201 Created）：
```json
{
  "success": true,
  "data": {
    "requestId": "req_xxxxxxxxxxxx",
    "email": "test@example.com",
    "userType": "creator",
    "status": "pending",
    "createdAt": "2026-01-08T..."
  },
  "message": "Demo request submitted successfully. We will contact you soon.",
  "timestamp": "2026-01-08T..."
}
```

#### 4.4 测试 Request Demo API - 验证错误
```bash
curl -X POST https://your-vercel-url.vercel.app/api/v1/demo-requests \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "userType": "invalid_type",
    "socialHandle": "ab",
    "socialPlatform": "invalid"
  }'
```

预期响应（400 Bad Request）：
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data",
    "details": [
      {
        "field": "email",
        "message": "Invalid email format"
      },
      ...
    ]
  },
  "timestamp": "2026-01-08T..."
}
```

#### 4.5 测试重复请求检测
```bash
# 第一次请求（应该成功）
curl -X POST https://your-vercel-url.vercel.app/api/v1/demo-requests \
  -H "Content-Type: application/json" \
  -d '{
    "email": "duplicate@example.com",
    "userType": "creator",
    "socialHandle": "test_handle",
    "socialPlatform": "telegram"
  }'

# 第二次请求（应该返回 409）
curl -X POST https://your-vercel-url.vercel.app/api/v1/demo-requests \
  -H "Content-Type: application/json" \
  -d '{
    "email": "duplicate@example.com",
    "userType": "creator",
    "socialHandle": "another_handle",
    "socialPlatform": "telegram"
  }'
```

预期响应（409 Conflict）：
```json
{
  "success": false,
  "error": {
    "code": "DUPLICATE_REQUEST",
    "message": "A demo request with this email already exists",
    "details": {
      "existingRequestId": "req_xxxxxxxxxxxx",
      "submittedAt": "2026-01-08T..."
    }
  },
  "timestamp": "2026-01-08T..."
}
```

## 验证清单

- [ ] Vercel 控制台显示最新部署为 "Ready"
- [ ] 健康检查端点返回 200 OK
- [ ] Swagger UI 可访问并显示 Request Demo section
- [ ] `POST /api/v1/demo-requests` 端点存在
- [ ] 有效请求返回 201 Created
- [ ] 验证错误返回 400 Bad Request
- [ ] 重复请求返回 409 Conflict
- [ ] 速率限制正常工作（需要多次请求测试）
- [ ] Swagger 文档包含所有新的 schemas

## 故障排除

### 如果部署未自动触发

1. 在 Vercel 控制台手动触发部署：
   - 进入项目设置
   - 点击 "Deployments"
   - 点击 "Redeploy" 或 "Deploy"

2. 检查 GitHub 集成：
   - 确保 Vercel 已连接到正确的 GitHub 仓库
   - 确保监听 `main` 分支

### 如果 API 返回 404

1. 检查 `vercel.json` 配置是否正确
2. 确保 `server.js` 正确导出 Express app
3. 检查 Vercel 构建日志是否有错误

### 如果 Swagger UI 不显示新端点

1. 清除浏览器缓存
2. 使用无痕模式访问
3. 检查 `swagger.yaml` 是否正确推送到 GitHub
4. 等待 1-2 分钟让 Vercel 完成部署

## 联系信息

如果遇到问题：
1. 检查 Vercel 构建日志
2. 检查 GitHub 提交是否成功
3. 验证网络连接到 GitHub
4. 查看 Vercel 项目设置

## 部署的文件

以下文件已更新并推送到 GitHub：
- ✅ `server.js` - 包含 Request Demo API 端点实现
- ✅ `swagger.yaml` - 包含 Request Demo API 文档
- ✅ `demo-requests.txt` - 数据存储文件
- ✅ `vercel.json` - Vercel 配置
