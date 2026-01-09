# Vercel 500 错误快速修复清单

## 🎯 目标
修复 `https://allweb3-mock-api.vercel.app/` 的 500 错误

## ⚡ 5 分钟快速修复

### ✅ 步骤 1: 检查根目录设置（最重要！）

1. **访问项目设置**
   - URL: https://vercel.com/allweb3/allweb3-mock-api/settings/general

2. **检查 Root Directory**
   - 如果显示: `BackEnd Endpoint/swagger-mock-api` → ✅ 正确
   - 如果显示其他路径 → ❌ 需要修改
   - 如果是空的 → 需要设置

3. **修复选项**
   - **选项 A**: 如果代码在仓库根目录，**清空 Root Directory**
   - **选项 B**: 如果代码在子目录，设置为: `BackEnd Endpoint/swagger-mock-api`
   - **注意**: 路径必须完全匹配，包括大小写和空格

### ✅ 步骤 2: 查看错误日志

1. **访问部署页面**
   - URL: https://vercel.com/allweb3/allweb3-mock-api

2. **查看最新部署**
   - 点击最新的部署
   - 查看 **Build Logs**（构建日志）
   - 查看 **Runtime Logs**（运行时日志）

3. **查找关键错误**
   - 查找 "Error" 或 "Failed" 关键字
   - 查找文件路径相关的错误
   - 查找依赖安装错误

### ✅ 步骤 3: 根据错误修复

#### 如果是 "Cannot find module" 或路径错误：
- 检查根目录设置
- 确认所有文件都在正确位置

#### 如果是 "swagger.yaml" 相关错误：
- 已更新代码添加错误处理
- 需要重新部署

#### 如果是依赖安装错误：
- 检查 `package.json`
- 确认所有依赖都正确列出

### ✅ 步骤 4: 重新部署

**方法 1: 自动部署（如果已推送代码）**
- 代码已推送到 GitHub
- Vercel 应该自动检测并部署
- 等待 1-2 分钟

**方法 2: 手动触发**
1. 在 Vercel Dashboard
2. 点击 **Deploy** → **Deploy latest commit**
3. 选择 **main** 分支
4. 点击 **Deploy**

### ✅ 步骤 5: 验证修复

按顺序测试以下端点：

1. **健康检查**
   ```
   https://allweb3-mock-api.vercel.app/health
   ```
   ✅ 应该返回: `{"status":"ok",...}`

2. **根路径**
   ```
   https://allweb3-mock-api.vercel.app/
   ```
   ✅ 应该返回 API 信息 JSON

3. **Swagger UI**
   ```
   https://allweb3-mock-api.vercel.app/docs/
   ```
   ✅ 应该显示 Swagger 文档界面

## 🔍 常见问题

### Q: 根目录应该设置什么？
**A**: 
- 如果代码在 `BackEnd Endpoint/swagger-mock-api` 子目录 → 设置为: `BackEnd Endpoint/swagger-mock-api`
- 如果代码在仓库根目录 → 留空

### Q: 如何确认代码位置？
**A**: 
- 查看 GitHub 仓库: https://github.com/Allweb3Labs/aw3-platform-mock-api
- 确认 `server.js` 和 `swagger.yaml` 的位置

### Q: 构建日志在哪里？
**A**: 
- Vercel Dashboard → 项目 → Deployments → 最新部署 → Build Logs

### Q: 运行时日志在哪里？
**A**: 
- Vercel Dashboard → 项目 → Deployments → 最新部署 → Runtime Logs 或 Function Logs

## 📝 已应用的修复

✅ 已更新 `server.js` 添加：
- swagger.yaml 加载错误处理
- 调试日志输出
- 降级方案（如果 swagger.yaml 无法加载）

✅ 代码已推送到 GitHub（提交: 77311c4）

## 🆘 如果仍然失败

1. **复制完整的错误信息**
   - 从 Runtime Logs 中复制
   - 包括错误堆栈

2. **检查文件结构**
   - 在构建日志中确认文件列表
   - 确认所有必需文件都存在

3. **尝试简化测试**
   - 使用 `server-vercel-fix.js` 作为临时测试
   - 如果简化版本可以工作，逐步添加功能

## 🔗 快速链接

- **项目设置**: https://vercel.com/allweb3/allweb3-mock-api/settings/general
- **部署列表**: https://vercel.com/allweb3/allweb3-mock-api
- **GitHub 仓库**: https://github.com/Allweb3Labs/aw3-platform-mock-api
