# Vercel Framework Preset 配置指南

## 问题说明

在 Vercel 创建新项目时，Framework Preset 选项中显示：
- Express
- Other
- Next.js

**没有 "Node.js" 选项**，这是正常的，因为 Vercel 将 Express 作为独立的框架选项。

## 解决方案

### 选择 "Express"（推荐）

由于项目使用 **Express.js** 框架，应该选择 **"Express"** preset。

#### 为什么选择 Express？

1. **项目使用 Express.js**
   - `server.js` 使用 `const express = require('express')`
   - `package.json` 包含 `"express": "^4.18.2"`

2. **Vercel 自动配置**
   - 选择 Express 后，Vercel 会自动识别 Express 应用
   - 会自动配置正确的构建和运行环境

3. **与 vercel.json 兼容**
   - 即使选择了 Express，`vercel.json` 中的配置仍然有效
   - Vercel 会优先使用 `vercel.json` 的配置

### 选择 "Other"（备选）

如果选择 **"Other"**，也可以正常工作，因为：
- 项目已有 `vercel.json` 配置文件
- Vercel 会自动检测并使用 `vercel.json` 中的配置
- `vercel.json` 中已指定使用 `@vercel/node`

## 完整配置步骤

### 步骤 1: 创建新项目

1. 访问 Vercel Dashboard: https://vercel.com/dashboard
2. 点击 **"Add New..."** → **"Project"**
3. 搜索并选择 GitHub 仓库: `Allweb3Labs/aw3-platform-mock-api`
4. 点击 **"Import"**

### 步骤 2: 配置项目设置

#### Framework Preset
- **选择**: **"Express"** ✅（推荐）
- 或选择: **"Other"**（也可以）

#### Project Name
- 输入: `swagger-mock-api-v2` 或自定义名称

#### Root Directory ⚠️ 重要
- **必须设置**: `BackEnd Endpoint/swagger-mock-api`
- 注意大小写和空格必须完全匹配
- 这是关键配置，错误会导致 500 错误

#### Build Command
- **留空**（Node.js/Express 项目通常不需要构建步骤）

#### Output Directory
- **留空**

#### Install Command
- 默认: `npm install`
- 通常不需要修改

#### Environment Variables
- 通常不需要额外环境变量
- 如果需要，可以稍后在 Settings 中添加

### 步骤 3: 部署

1. 点击 **"Deploy"** 按钮
2. 等待部署完成（1-2 分钟）
3. 查看部署日志确认成功

### 步骤 4: 验证

部署完成后，访问：
- **Swagger UI**: `https://[项目名称].vercel.app/docs/`
- **健康检查**: `https://[项目名称].vercel.app/health`

确认：
- ✅ 页面正常加载
- ✅ Request Demo section 在 Creator Profile 之前
- ✅ API 端点可正常访问

## vercel.json 配置说明

项目中的 `vercel.json` 配置：

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
  ]
}
```

### 配置说明

- **version**: 2 - 使用 Vercel 配置格式版本 2
- **builds**: 指定构建配置
  - `src: "server.js"` - 入口文件
  - `use: "@vercel/node"` - 使用 Vercel Node.js runtime
- **routes**: 路由配置
  - 所有请求 (`(.*)`) 都路由到 `server.js`

### 与 Framework Preset 的关系

- 选择 **Express** preset 不会覆盖 `vercel.json`
- Vercel 会优先使用 `vercel.json` 中的配置
- Framework preset 主要用于初始配置和自动检测

## 常见问题

### Q: 为什么没有 "Node.js" 选项？

**A**: Vercel 将 Node.js 框架（如 Express、Next.js）作为独立选项。由于项目使用 Express.js，应该选择 "Express"。

### Q: 选择 Express 还是 Other？

**A**: 
- **推荐选择 Express**: 更明确，Vercel 会自动优化 Express 应用
- **选择 Other 也可以**: 因为已有 `vercel.json`，配置会正常工作

### Q: Root Directory 必须设置吗？

**A**: **是的，必须设置**。因为代码在子目录 `BackEnd Endpoint/swagger-mock-api` 中，不设置会导致 Vercel 找不到 `server.js` 文件，出现 500 错误。

### Q: 如果 Root Directory 设置错误怎么办？

**A**: 
1. 进入项目 Settings → General
2. 修改 Root Directory
3. 保存设置
4. 重新部署

### Q: Build Command 应该设置什么？

**A**: **留空**。Node.js/Express 项目通常不需要构建步骤，直接运行 `node server.js` 即可。

### Q: 部署后出现 500 错误？

**A**: 检查以下几点：
1. Root Directory 设置是否正确
2. 查看 Build Logs 和 Runtime Logs
3. 确认 `server.js` 文件存在
4. 确认 `package.json` 存在
5. 确认依赖安装成功

## 配置检查清单

创建项目时，确认以下配置：

- [ ] Framework Preset: **Express** 或 **Other**
- [ ] Root Directory: `BackEnd Endpoint/swagger-mock-api`（必须）
- [ ] Build Command: **留空**
- [ ] Output Directory: **留空**
- [ ] Install Command: `npm install`（默认）
- [ ] 连接到正确的 GitHub 仓库
- [ ] 监听 `main` 分支

## 相关文件

- `vercel.json` - Vercel 配置文件
- `server.js` - Express 应用入口
- `package.json` - 项目依赖配置

## 参考链接

- **Vercel Dashboard**: https://vercel.com/dashboard
- **Vercel 文档 - Express**: https://vercel.com/docs/frameworks/express
- **Vercel 文档 - 配置**: https://vercel.com/docs/project-configuration
