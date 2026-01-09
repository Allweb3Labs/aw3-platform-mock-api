# Vercel Token 设置指南

## 如何获取 Vercel Token

### 步骤 1: 访问 Token 设置页面

**直接链接：** https://vercel.com/account/tokens

或者通过以下步骤：
1. 登录 Vercel Dashboard: https://vercel.com/dashboard
2. 点击右上角的头像/账户图标
3. 选择 "Settings"（设置）
4. 在左侧菜单中找到 "Tokens"（令牌）
5. 点击进入 Token 管理页面

### 步骤 2: 创建新的 Token

1. 在 Token 页面，点击 **"Create Token"** 按钮
2. 填写 Token 信息：
   - **Name（名称）**: 例如 "Swagger API Auto Deploy" 或 "Auto Deployment Token"
   - **Expiration（过期时间）**: 
     - 选择 "No Expiration"（永不过期）用于长期使用
     - 或选择具体日期设置过期时间
   - **Scope（范围）**: 
     - 选择 "Full Account"（完整账户访问）以确保有部署权限
     - 或根据需要选择特定权限

3. 点击 **"Create Token"** 创建

### 步骤 3: 复制 Token

⚠️ **重要：Token 只会显示一次！**

创建成功后，Vercel 会显示您的 Token。请立即复制并安全保存。

**Token 格式示例：**
```
vercel_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### 步骤 4: 使用 Token 触发部署

#### 方法 1: 使用 PowerShell 脚本（推荐）

1. 将 Token 保存到环境变量（更安全）：
```powershell
$env:VERCEL_TOKEN = "your_vercel_token_here"
```

2. 运行部署脚本：
```powershell
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"
.\trigger-vercel-deployment.ps1 -VercelToken $env:VERCEL_TOKEN
```

#### 方法 2: 直接在命令行中提供 Token

```powershell
.\trigger-vercel-deployment.ps1 -VercelToken "vercel_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
```

#### 方法 3: 使用 curl（跨平台）

```bash
curl -X POST "https://api.vercel.com/v13/deployments" \
  -H "Authorization: Bearer YOUR_VERCEL_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "swagger-mock-api",
    "project": "swagger-mock-api",
    "target": "production"
  }'
```

## 安全注意事项

### ⚠️ Token 安全

1. **不要将 Token 提交到 Git**
   - 不要将 Token 写入代码文件
   - 不要将 Token 提交到 GitHub
   - 使用环境变量或配置文件（添加到 .gitignore）

2. **Token 权限**
   - 只授予必要的权限
   - 定期轮换 Token
   - 如果 Token 泄露，立即撤销

3. **Token 存储**
   - 使用环境变量存储
   - 使用密码管理器
   - 不要分享给不信任的人

### 创建 .env 文件（推荐）

1. 在项目根目录创建 `.env` 文件：
```
VERCEL_TOKEN=vercel_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

2. 确保 `.env` 在 `.gitignore` 中：
```
.env
*.env
```

3. 在脚本中读取：
```powershell
# 读取 .env 文件（需要安装 dotenv 或手动解析）
$envContent = Get-Content .env
$env:VERCEL_TOKEN = ($envContent | Where-Object { $_ -match "^VERCEL_TOKEN=" }) -replace "VERCEL_TOKEN=", ""
```

## 自动化部署流程

### 完整自动化脚本

创建一个完整的自动化脚本，包括：
1. 检查 Git 状态
2. 提交更改（如果需要）
3. 推送到 GitHub
4. 触发 Vercel 部署
5. 等待部署完成
6. 验证部署结果

示例脚本：`auto-deploy-vercel.ps1`

## 故障排除

### Token 无效错误

如果收到 "Invalid token" 错误：
1. 检查 Token 是否正确复制（没有多余空格）
2. 确认 Token 未过期
3. 确认 Token 有正确的权限

### 权限不足错误

如果收到 "Insufficient permissions" 错误：
1. 检查 Token 的 Scope 设置
2. 确认 Token 有部署权限
3. 确认项目属于正确的 Team

### 项目未找到错误

如果收到 "Project not found" 错误：
1. 检查项目名称是否正确
2. 确认 Team 名称是否正确
3. 确认 Token 属于正确的账户

## 相关链接

- **Vercel Token 管理**: https://vercel.com/account/tokens
- **Vercel API 文档**: https://vercel.com/docs/rest-api
- **Vercel Deployments API**: https://vercel.com/docs/rest-api#endpoints/deployments
- **Vercel Dashboard**: https://vercel.com/dashboard

## 快速参考

### Token 申请链接
**直接访问：** https://vercel.com/account/tokens

### 常用 API 端点

1. **获取项目列表**
   ```
   GET https://api.vercel.com/v9/projects
   ```

2. **获取部署列表**
   ```
   GET https://api.vercel.com/v6/deployments?projectId={projectId}
   ```

3. **创建新部署**
   ```
   POST https://api.vercel.com/v13/deployments
   ```

4. **取消部署**
   ```
   PATCH https://api.vercel.com/v13/deployments/{deploymentId}/cancel
   ```

## 下一步

1. ✅ 访问 https://vercel.com/account/tokens 创建 Token
2. ✅ 复制 Token 并安全保存
3. ✅ 运行 `trigger-vercel-deployment.ps1` 脚本
4. ✅ 在 Vercel Dashboard 中验证部署
