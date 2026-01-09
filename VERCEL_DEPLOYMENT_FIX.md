# Vercel 部署错误修复指南

## 🔴 当前错误
```
500: INTERNAL_SERVER_ERROR
Code: FUNCTION_INVOCATION_FAILED
```

## 🔍 问题诊断

### 最可能的原因
1. **根目录设置问题** - 如果设置了根目录，路径可能不正确
2. **swagger.yaml 文件加载失败** - 文件路径或大小问题
3. **依赖安装问题** - 某些依赖未正确安装

## ✅ 修复步骤

### 步骤 1: 检查 Vercel 项目设置

1. **访问 Vercel Dashboard**
   - URL: https://vercel.com/dashboard
   - 找到项目: `allweb3-mock-api`

2. **检查根目录设置**
   - 进入 **Settings** → **General**
   - 查看 **Root Directory** 设置
   
   **重要**: 
   - 如果代码在子目录，路径应该是: `BackEnd Endpoint/swagger-mock-api`
   - 注意大小写和空格必须完全匹配
   - 或者，如果代码在仓库根目录，**移除根目录设置**

### 步骤 2: 查看构建和运行时日志

1. **查看构建日志**
   - 进入 **Deployments** → 最新部署
   - 点击 **Build Logs**
   - 查找错误信息

2. **查看运行时日志**
   - 在部署详情页面
   - 点击 **Runtime Logs** 或 **Function Logs**
   - 查看具体的错误信息

### 步骤 3: 根据错误信息修复

#### 如果是路径问题：
- **选项 A**: 移除根目录设置（如果代码在仓库根目录）
- **选项 B**: 确认根目录路径完全正确

#### 如果是 swagger.yaml 加载失败：
- 已更新 `server.js` 添加错误处理
- 需要重新部署以应用更改

### 步骤 4: 重新部署

1. **推送代码更新**
   ```bash
   git push origin main
   ```
   Vercel 会自动检测并部署

2. **或手动触发部署**
   - 在 Vercel Dashboard 中
   - 点击 **Deploy** → **Deploy latest commit**

## 🛠️ 已应用的修复

### 1. 更新了 server.js
- 添加了 swagger.yaml 加载的错误处理
- 添加了调试日志
- 添加了降级方案（如果 swagger.yaml 无法加载）

### 2. 创建了测试版本
- `server-vercel-fix.js` - 带完整错误处理的版本

## 📋 验证步骤

部署完成后，按顺序测试：

1. **健康检查**
   ```
   https://allweb3-mock-api.vercel.app/health
   ```
   应该返回 JSON 响应

2. **根路径**
   ```
   https://allweb3-mock-api.vercel.app/
   ```
   应该返回 API 信息

3. **Swagger UI**
   ```
   https://allweb3-mock-api.vercel.app/docs/
   ```
   应该显示 Swagger 文档界面

## 🔍 调试信息

如果仍然失败，检查运行时日志中的以下信息：
- `Loading swagger.yaml from: [路径]`
- `Current working directory: [路径]`
- `__dirname: [路径]`
- 任何错误消息

这些信息会帮助确定路径问题。

## 🆘 如果仍然失败

### 选项 1: 使用简化版本测试
临时使用 `server-vercel-fix.js` 替换 `server.js` 来测试基本功能。

### 选项 2: 检查文件结构
在 Vercel 构建日志中，确认文件结构是否正确：
```
/
├── server.js
├── swagger.yaml
├── package.json
├── vercel.json
└── node_modules/
```

### 选项 3: 联系支持
如果问题持续，查看 Vercel 支持文档或联系支持。

## 🔗 相关链接

- **Vercel Dashboard**: https://vercel.com/dashboard
- **项目设置**: https://vercel.com/allweb3/allweb3-mock-api/settings
- **部署日志**: https://vercel.com/allweb3/allweb3-mock-api
