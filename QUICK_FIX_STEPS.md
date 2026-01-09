# 快速修复 Vercel 部署 - 操作步骤

## 🎯 目标
修复 Vercel 上的 Swagger API 部署，确保 Request Demo section 显示在 Creator Profile 之前。

## ⚡ 快速操作步骤（5 分钟）

### 步骤 1: 访问 Vercel Dashboard
👉 **直接链接**: https://vercel.com/allweb3/swagger-mock-api

### 步骤 2: 清除构建缓存
1. 在项目页面，点击 **"Settings"**（设置）
2. 在左侧菜单找到 **"Git"** 或 **"Build & Development Settings"**
3. 滚动到底部，找到 **"Clear Build Cache"** 按钮
4. 点击 **"Clear Build Cache"** 并确认

### 步骤 3: 触发重新部署
**方法 A: 从部署列表**
1. 点击 **"Deployments"** 标签页
2. 找到最新的部署（通常是第一个）
3. 点击部署右侧的 **"..."** 菜单
4. 选择 **"Redeploy"**
5. 确认重新部署

**方法 B: 部署最新提交**
1. 点击页面右上角的 **"Deploy"** 按钮
2. 选择 **"Deploy latest commit"**
3. 选择 **"main"** 分支
4. 点击 **"Deploy"**

### 步骤 4: 等待部署完成
- 部署通常需要 1-2 分钟
- 在 "Deployments" 页面可以看到部署进度
- 等待状态变为 **"Ready"**（绿色）

### 步骤 5: 验证结果
1. 访问 Swagger UI: https://swagger-mock-api-five.vercel.app/docs/
2. 使用 **无痕模式** 或 **硬刷新** (Ctrl+F5) 访问
3. 确认：
   - ✅ "Request Demo" section 出现在最前面
   - ✅ "Creator Profile" section 在 Request Demo 之后
   - ✅ `POST /api/v1/demo-requests` 端点可见

## 🔍 如果仍未更新

### 检查部署日志
1. 在 Vercel Dashboard 中点击最新部署
2. 查看 **"Build Logs"** 和 **"Runtime Logs"**
3. 检查是否有错误信息

### 检查部署的代码版本
1. 在部署详情页面，查看 **"Source"** 部分
2. 确认提交 SHA 是 `456af2a` 或更新
3. 如果不是，说明部署的是旧版本

### 强制重新部署
如果部署的代码版本不对：
1. 在部署详情页面
2. 点击 **"Redeploy"**
3. 选择 **"Use existing Build Cache"** = **OFF**
4. 确认部署

## 📝 验证清单

- [ ] 已清除构建缓存
- [ ] 已触发重新部署
- [ ] 部署状态为 "Ready"
- [ ] 部署的提交是 `456af2a` 或更新
- [ ] Swagger UI 显示 Request Demo 在 Creator Profile 之前
- [ ] 使用无痕模式验证（排除缓存问题）

## 🆘 仍然有问题？

如果按照以上步骤操作后仍未更新：

1. **检查 GitHub 仓库**
   - 确认代码已正确推送到 GitHub
   - 访问: https://github.com/Allweb3Labs/aw3-platform-mock-api
   - 检查 `swagger.yaml` 文件中的顺序

2. **检查 Vercel 项目配置**
   - 确认项目连接到正确的 GitHub 仓库
   - 确认监听的分支是 `main`
   - 确认自动部署已启用

3. **联系支持**
   - 查看 Vercel 构建日志中的错误信息
   - 或联系 Vercel 支持

## 🔗 相关链接

- **Vercel Dashboard**: https://vercel.com/allweb3/swagger-mock-api
- **部署详情**: https://vercel.com/allweb3/swagger-mock-api/2AvM7UkgAVSqJiHfym9743A2eBMQ
- **Swagger UI**: https://swagger-mock-api-five.vercel.app/docs/
- **GitHub 仓库**: https://github.com/Allweb3Labs/aw3-platform-mock-api
