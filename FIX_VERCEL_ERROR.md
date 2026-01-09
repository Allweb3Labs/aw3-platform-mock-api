# 修复 Vercel 部署错误 - 500 Internal Server Error

## 🔴 错误信息
```
This Serverless Function has crashed.
500: INTERNAL_SERVER_ERROR
Code: FUNCTION_INVOCATION_FAILED
```

## 🔍 可能的原因

### 1. 根目录设置问题（最可能）
如果设置了根目录 `BackEnd Endpoint/swagger-mock-api`，Vercel 会在该目录下查找文件，但 `vercel.json` 中的路径可能需要调整。

### 2. swagger.yaml 文件路径问题
`yamljs` 可能无法找到 `swagger.yaml` 文件。

### 3. 依赖安装问题
某些依赖可能未正确安装。

## ✅ 解决方案

### 方案 1: 修复根目录设置（推荐）

#### 步骤 1: 检查 Vercel 项目设置
1. 访问 Vercel Dashboard: https://vercel.com/dashboard
2. 找到项目 `allweb3-mock-api`
3. 进入 **Settings** → **General**

#### 步骤 2: 检查根目录设置
- 如果设置了根目录，有两个选择：

**选项 A: 移除根目录设置（如果代码在仓库根目录）**
- 将 **Root Directory** 设置为空
- 保存设置
- 重新部署

**选项 B: 确认根目录路径正确**
- 确认 **Root Directory** 是: `BackEnd Endpoint/swagger-mock-api`
- 注意：路径区分大小写，确保完全匹配

#### 步骤 3: 检查构建日志
1. 进入 **Deployments** 页面
2. 点击最新的部署
3. 查看 **Build Logs**
4. 查找错误信息

### 方案 2: 修改 vercel.json（如果根目录已设置）

如果根目录设置为 `BackEnd Endpoint/swagger-mock-api`，可能需要调整 `vercel.json`：

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

这个配置应该可以工作，但确保 `server.js` 在根目录设置后的路径中。

### 方案 3: 检查 server.js 中的路径

确保 `server.js` 中使用相对路径：

```javascript
const swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
```

这应该可以工作，因为 `__dirname` 会指向正确的目录。

### 方案 4: 添加错误处理和日志

修改 `server.js` 以添加更好的错误处理：

```javascript
// Load Swagger document with error handling
let swaggerDocument;
try {
  swaggerDocument = YAML.load(path.join(__dirname, 'swagger.yaml'));
} catch (error) {
  console.error('Failed to load swagger.yaml:', error);
  console.error('__dirname:', __dirname);
  console.error('Current working directory:', process.cwd());
  // 使用默认的简单文档
  swaggerDocument = {
    openapi: '3.0.0',
    info: { title: 'API', version: '1.0.0' },
    paths: {}
  };
}
```

## 🛠️ 快速修复步骤

### 步骤 1: 检查 Vercel 项目设置
1. 访问: https://vercel.com/dashboard
2. 找到项目: `allweb3-mock-api`
3. 进入 **Settings** → **General**

### 步骤 2: 检查根目录
- 如果 **Root Directory** 已设置，确认路径正确
- 路径应该是: `BackEnd Endpoint/swagger-mock-api`
- 注意大小写和空格

### 步骤 3: 查看构建日志
1. 进入 **Deployments**
2. 点击最新部署
3. 查看 **Build Logs** 和 **Runtime Logs**
4. 查找具体错误信息

### 步骤 4: 根据错误信息修复
- 如果是路径问题：调整根目录设置
- 如果是依赖问题：检查 `package.json`
- 如果是文件缺失：确认所有文件都在正确位置

## 📋 检查清单

- [ ] 根目录设置正确（或为空）
- [ ] `server.js` 文件存在
- [ ] `swagger.yaml` 文件存在
- [ ] `package.json` 文件存在
- [ ] `vercel.json` 文件存在
- [ ] 构建日志中没有错误
- [ ] 运行时日志显示具体错误

## 🔍 调试步骤

### 1. 查看构建日志
在 Vercel Dashboard 中：
- Deployments → 最新部署 → Build Logs
- 查看是否有构建错误

### 2. 查看运行时日志
- Deployments → 最新部署 → Runtime Logs
- 查看函数执行时的错误

### 3. 测试健康检查端点
访问: `https://allweb3-mock-api.vercel.app/health`
- 如果这个也失败，说明是基本配置问题
- 如果这个成功，说明是特定路由问题

### 4. 检查文件结构
确认在 Vercel 中，文件结构是：
```
/
├── server.js
├── swagger.yaml
├── package.json
├── vercel.json
└── node_modules/
```

## 🆘 如果仍然失败

### 选项 1: 重新创建项目（不设置根目录）
1. 删除当前项目
2. 创建新项目
3. **不设置根目录**（如果代码在仓库根目录）
4. 或者将代码移动到仓库根目录

### 选项 2: 使用 Vercel CLI 本地测试
```bash
npm i -g vercel
vercel login
vercel dev
```
这会在本地模拟 Vercel 环境，可以看到具体错误。

### 选项 3: 简化 server.js
创建一个最小版本测试：
```javascript
const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.json({ message: 'API is working' });
});

module.exports = app;
```

如果这个可以工作，再逐步添加功能。

## 📝 需要的信息

为了进一步诊断，请提供：
1. Vercel Dashboard 中的构建日志
2. 运行时日志中的错误信息
3. 根目录设置（如果有）
4. 项目设置截图

## 🔗 相关链接

- **Vercel Dashboard**: https://vercel.com/dashboard
- **项目设置**: https://vercel.com/allweb3/allweb3-mock-api/settings
- **部署日志**: https://vercel.com/allweb3/allweb3-mock-api
