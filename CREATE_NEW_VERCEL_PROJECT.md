# 创建新的 Vercel 项目 - 完整指南

## 🎯 目标
创建一个新的 Vercel 项目，连接到同一个 GitHub 仓库，自动包含最新的 Request Demo API。

## ⚡ 快速操作步骤（10 分钟）

### 方法 1: 通过 Vercel Dashboard（推荐）

#### 步骤 1: 访问 Vercel Dashboard
👉 **链接**: https://vercel.com/dashboard

#### 步骤 2: 创建新项目
1. 点击右上角的 **"Add New..."** 按钮
2. 选择 **"Project"**

#### 步骤 3: 导入 GitHub 仓库
1. 在 "Import Git Repository" 页面
2. 搜索或选择: **`Allweb3Labs/aw3-platform-mock-api`**
3. 点击 **"Import"**

#### 步骤 4: 配置项目
1. **Project Name（项目名称）**: 
   - 输入: `swagger-mock-api-v2` 或 `swagger-api-new`
   - 或使用默认名称

2. **Framework Preset（框架预设）**:
   - 选择: **"Other"** 或 **"Node.js"**

3. **Root Directory（根目录）**:
   - 如果代码在子目录: `BackEnd Endpoint/swagger-mock-api`
   - 如果代码在根目录: 留空

4. **Build Command（构建命令）**:
   - 留空（Node.js 项目通常不需要构建）

5. **Output Directory（输出目录）**:
   - 留空

6. **Install Command（安装命令）**:
   - 默认: `npm install`

#### 步骤 5: 环境变量（如果需要）
- 通常不需要额外环境变量
- 如果需要，可以稍后在 Settings 中添加

#### 步骤 6: 部署
1. 点击 **"Deploy"** 按钮
2. 等待部署完成（1-2 分钟）

#### 步骤 7: 获取新项目 URL
部署完成后，您会看到：
- **Production URL**: `https://swagger-mock-api-v2.vercel.app`（或类似）
- **Swagger UI**: `https://swagger-mock-api-v2.vercel.app/docs/`

### 方法 2: 使用 Vercel CLI

```bash
# 1. 安装 Vercel CLI（如果未安装）
npm i -g vercel

# 2. 登录 Vercel
vercel login

# 3. 进入项目目录
cd "A:\Web3\Allweb3 PM\Back-End\BackEnd Endpoint\swagger-mock-api"

# 4. 创建新项目
vercel

# 5. 按照提示操作：
#    - 选择团队: allweb3
#    - 项目名称: swagger-mock-api-v2
#    - 是否覆盖设置: No
#    - 部署到生产: Yes

# 6. 部署到生产环境
vercel --prod
```

## ✅ 验证新项目

### 1. 检查部署状态
- 访问 Vercel Dashboard
- 查看新项目的部署状态
- 确认状态为 "Ready"（绿色）

### 2. 访问 Swagger UI
访问新项目的 Swagger UI:
- `https://[新项目名称].vercel.app/docs/`

### 3. 验证 Request Demo Section
确认：
- ✅ "Request Demo" section 出现在最前面
- ✅ "Creator Profile" section 在 Request Demo 之后
- ✅ `POST /api/v1/demo-requests` 端点可见并可测试

### 4. 测试 API 端点
在 Swagger UI 中测试 Request Demo API:
1. 展开 `POST /api/v1/demo-requests`
2. 点击 "Try it out"
3. 填写测试数据：
   ```json
   {
     "email": "test@example.com",
     "userType": "creator",
     "socialHandle": "test_handle",
     "socialPlatform": "telegram"
   }
   ```
4. 点击 "Execute"
5. 确认返回 201 状态码

## 🔄 后续操作

### 选项 1: 保留两个项目
- 旧项目: `swagger-mock-api` (https://swagger-mock-api-five.vercel.app)
- 新项目: `swagger-mock-api-v2` (新 URL)
- 两个项目都连接到同一个 GitHub 仓库
- 新项目会自动使用最新代码

### 选项 2: 替换旧项目
1. 确认新项目工作正常
2. 在旧项目中更新自定义域名（如果有）
3. 删除或归档旧项目

### 选项 3: 使用自定义域名
1. 在新项目的 Settings → Domains
2. 添加您的自定义域名
3. 按照提示配置 DNS

## 📋 项目配置检查清单

创建新项目后，确认以下配置：

- [ ] 项目名称正确
- [ ] 连接到正确的 GitHub 仓库
- [ ] 监听 `main` 分支
- [ ] 自动部署已启用
- [ ] 根目录设置正确（如果代码在子目录）
- [ ] 部署成功完成
- [ ] Swagger UI 可访问
- [ ] Request Demo section 显示正确

## 🆘 常见问题

### 问题 1: 找不到 GitHub 仓库
**解决**: 
- 确认已授权 Vercel 访问 GitHub
- 在 Vercel Settings → Git 中检查连接

### 问题 2: 部署失败
**检查**:
- 查看构建日志
- 确认根目录设置正确
- 检查 `package.json` 和依赖

### 问题 3: 代码未更新
**解决**:
- 确认连接到正确的分支（main）
- 检查 GitHub 仓库中的代码是否最新
- 手动触发重新部署

## 🔗 相关链接

- **Vercel Dashboard**: https://vercel.com/dashboard
- **GitHub 仓库**: https://github.com/Allweb3Labs/aw3-platform-mock-api
- **旧项目**: https://vercel.com/allweb3/swagger-mock-api
- **Vercel 文档**: https://vercel.com/docs

## 📝 新项目信息记录

创建新项目后，请记录以下信息：

- **项目名称**: ________________
- **项目 URL**: https://________________.vercel.app
- **Swagger UI**: https://________________.vercel.app/docs/
- **Vercel Dashboard**: https://vercel.com/allweb3/________________
