# 部署平台对比指南

## 平台概览

项目支持部署到以下平台：
1. **Vercel** - Serverless 函数平台
2. **Render** - 传统 Web 服务平台
3. **Railway** - 现代化部署平台

## 平台对比

### Vercel

#### 优势
- ✅ **Serverless 架构** - 自动扩展，按需计费
- ✅ **全球 CDN** - 自动内容分发，低延迟
- ✅ **自动 HTTPS** - 免费 SSL 证书
- ✅ **零配置部署** - 自动检测框架
- ✅ **预览部署** - 每个 PR 自动创建预览
- ✅ **快速部署** - 通常 < 1 分钟
- ✅ **免费额度** - 个人项目免费使用

#### 劣势
- ⚠️ **Serverless 限制** - 函数执行时间限制（10 秒免费版）
- ⚠️ **冷启动** - 首次请求可能较慢
- ⚠️ **文件系统** - 只读文件系统（需要使用外部存储）
- ⚠️ **配置复杂** - 需要 `vercel.json` 配置

#### 适用场景
- API 端点
- 静态网站
- Serverless 函数
- 需要全球 CDN 的应用

#### 配置要求
- Framework Preset: **Express** 或 **Other**
- Root Directory: `BackEnd Endpoint/swagger-mock-api`
- Build Command: 留空
- vercel.json 配置文件

#### 当前状态
- ✅ 已有 `vercel.json` 配置
- ✅ 已配置 serverless 函数
- ⚠️ 需要正确设置 Root Directory

---

### Render

#### 优势
- ✅ **传统架构** - 熟悉的 Web 服务模式
- ✅ **持久化存储** - 可写文件系统
- ✅ **简单配置** - 自动检测 Node.js
- ✅ **已有配置** - 项目已有 `render.yaml`
- ✅ **成功经验** - 之前成功部署过
- ✅ **免费额度** - 免费 tier 可用
- ✅ **健康检查** - 自动健康监控

#### 劣势
- ⚠️ **启动时间** - 服务启动需要时间
- ⚠️ **单区域** - 没有全球 CDN
- ⚠️ **资源限制** - 免费版有资源限制
- ⚠️ **休眠** - 免费服务可能休眠

#### 适用场景
- 传统 Node.js 应用
- 需要文件系统写入的应用
- 长时间运行的服务
- 需要持久化存储的应用

#### 配置要求
- Root Directory: `BackEnd Endpoint/swagger-mock-api`
- Build Command: `npm install`
- Start Command: `npm start`
- render.yaml 配置文件

#### 当前状态
- ✅ 已有 `render.yaml` 配置
- ✅ 之前成功部署过
- ✅ 配置简单直接

---

### Railway

#### 优势
- ✅ **自动检测** - 自动检测项目类型
- ✅ **简单部署** - 一键部署
- ✅ **现代化** - 新的部署平台
- ✅ **灵活配置** - 支持多种配置方式
- ✅ **已有配置** - 项目已有 `railway.json`
- ✅ **实时日志** - 实时查看日志
- ✅ **环境变量** - 简单管理环境变量

#### 劣势
- ⚠️ **相对较新** - 平台较新，文档可能不完整
- ⚠️ **免费额度** - 免费额度有限
- ⚠️ **学习曲线** - 需要了解 Railway 概念

#### 适用场景
- 快速原型开发
- 需要灵活配置的应用
- 现代化部署流程
- 需要实时监控的应用

#### 配置要求
- Root Directory: `BackEnd Endpoint/swagger-mock-api`（如果需要）
- railway.json 配置文件（可选）
- 自动检测 package.json

#### 当前状态
- ✅ 已有 `railway.json` 配置
- ✅ 配置简单
- ✅ 自动检测功能完善

---

## 详细对比表

| 特性 | Vercel | Render | Railway |
|------|--------|--------|---------|
| **架构** | Serverless | Web Service | Container |
| **部署速度** | ⚡ 很快 (<1min) | 🐢 中等 (2-5min) | ⚡ 快 (1-3min) |
| **全球 CDN** | ✅ 是 | ❌ 否 | ❌ 否 |
| **文件系统** | ❌ 只读 | ✅ 可写 | ✅ 可写 |
| **自动扩展** | ✅ 是 | ⚠️ 手动 | ✅ 是 |
| **免费额度** | ✅ 充足 | ✅ 可用 | ⚠️ 有限 |
| **配置复杂度** | ⚠️ 中等 | ✅ 简单 | ✅ 简单 |
| **已有配置** | ✅ vercel.json | ✅ render.yaml | ✅ railway.json |
| **适合本项目** | ✅ 是 | ✅ 是 | ✅ 是 |

## 推荐方案

### 首选：Vercel（选择 Express framework）

**推荐理由**：
1. ✅ 已有 `vercel.json` 配置
2. ✅ 适合 API 服务
3. ✅ 全球 CDN，访问速度快
4. ✅ 自动 HTTPS
5. ✅ 免费额度充足

**配置要点**：
- Framework: **Express**
- Root Directory: `BackEnd Endpoint/swagger-mock-api`
- Build Command: 留空

### 备选 1：Render

**推荐理由**：
1. ✅ 已有成功部署经验
2. ✅ 配置简单（已有 render.yaml）
3. ✅ 适合传统 Node.js 应用
4. ✅ 文件系统可写（适合 demo-requests.txt）

**配置要点**：
- Root Directory: `BackEnd Endpoint/swagger-mock-api`
- Build: `npm install`
- Start: `npm start`

### 备选 2：Railway

**推荐理由**：
1. ✅ 已有 railway.json 配置
2. ✅ 自动检测配置
3. ✅ 现代化平台
4. ✅ 部署简单

**配置要点**：
- 自动检测，通常不需要额外配置
- 可能需要设置 Root Directory

## 部署步骤对比

### Vercel 部署步骤

1. 访问 https://vercel.com/dashboard
2. 点击 "Add New..." → "Project"
3. 导入 GitHub 仓库
4. 配置：
   - Framework: **Express**
   - Root Directory: `BackEnd Endpoint/swagger-mock-api`
   - Build Command: 留空
5. 点击 "Deploy"

**时间**: ~2 分钟

### Render 部署步骤

1. 访问 https://dashboard.render.com
2. 点击 "New +" → "Web Service"
3. 连接 GitHub 仓库
4. 配置：
   - Name: `swagger-mock-api-v2`
   - Root Directory: `BackEnd Endpoint/swagger-mock-api`
   - Environment: Node
   - Build Command: `npm install`
   - Start Command: `npm start`
5. 点击 "Create Web Service"

**时间**: ~3-5 分钟

### Railway 部署步骤

1. 访问 https://railway.app
2. 点击 "New Project"
3. 选择 "Deploy from GitHub repo"
4. 选择仓库: `Allweb3Labs/aw3-platform-mock-api`
5. Railway 自动检测配置
6. 如果需要，设置 Root Directory

**时间**: ~2-3 分钟

## 项目特定考虑

### demo-requests.txt 文件存储

- **Vercel**: ⚠️ 只读文件系统，需要使用外部存储（如数据库）
- **Render**: ✅ 可写文件系统，可以直接使用文件存储
- **Railway**: ✅ 可写文件系统，可以直接使用文件存储

**当前实现**: 使用文件系统存储 `demo-requests.txt`

**建议**:
- 如果使用 Vercel，考虑迁移到数据库或外部存储
- 如果使用 Render 或 Railway，当前实现可以直接使用

### Swagger UI 加载

所有平台都支持：
- ✅ Swagger UI 通过 CDN 加载（server.js 中已配置）
- ✅ swagger.yaml 文件读取
- ✅ API 端点路由

## 成本对比

### Vercel
- **免费版**: 
  - 100GB 带宽/月
  - 100 小时函数执行/月
  - 适合中小型项目

### Render
- **免费版**: 
  - 750 小时/月
  - 服务可能休眠（15 分钟无活动）
  - 适合开发/测试

### Railway
- **免费版**: 
  - $5 免费额度/月
  - 超出后按使用付费
  - 适合小型项目

## 迁移建议

### 从 Vercel 迁移到 Render

1. 在 Render 创建新服务
2. 连接同一个 GitHub 仓库
3. 设置 Root Directory
4. 部署后更新前端 API URL

### 从 Render 迁移到 Vercel

1. 在 Vercel 创建新项目
2. 选择 Express framework
3. 设置 Root Directory
4. 注意文件系统限制（demo-requests.txt 需要外部存储）

## 最终推荐

### 对于本项目

**推荐顺序**：
1. **Vercel**（选择 Express）- 如果不需要文件写入
2. **Render** - 如果需要文件写入（demo-requests.txt）
3. **Railway** - 如果需要更灵活的配置

### 决策矩阵

| 需求 | 推荐平台 |
|------|----------|
| 最快部署 | Vercel |
| 最简单配置 | Render |
| 文件系统写入 | Render 或 Railway |
| 全球 CDN | Vercel |
| 免费额度 | Vercel 或 Render |
| 已有配置 | 所有平台都有 |

## 相关文件

- `vercel.json` - Vercel 配置
- `render.yaml` - Render 配置
- `railway.json` - Railway 配置
- `server.js` - Express 应用
- `package.json` - 项目依赖

## 参考链接

- **Vercel 文档**: https://vercel.com/docs
- **Render 文档**: https://render.com/docs
- **Railway 文档**: https://docs.railway.app
