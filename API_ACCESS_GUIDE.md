# API 访问指南

## 部署地址

**生产环境**: https://swagger-mock-api-five.vercel.app

## 访问端点说明

### 1. 根路径 `/` (返回 JSON 信息)
**URL**: https://swagger-mock-api-five.vercel.app/

**说明**: 这是正常的 API 响应，返回 API 的基本信息（JSON 格式）

**响应示例**:
```json
{
  "name": "AW3 Platform Mock API",
  "version": "1.0.0",
  "documentation": "/docs",
  "endpoints": {
    "swagger": {
      "yaml": "/swagger.yaml",
      "json": "/swagger.json",
      "ui": "/docs"
    },
    "health": "/health"
  }
}
```

### 2. Swagger UI 文档 ⭐ (主要访问点)
**URL**: https://swagger-mock-api-five.vercel.app/docs

**说明**: 这是交互式的 API 文档界面，可以：
- 查看所有 API 端点
- 测试 API 端点
- 查看请求/响应示例
- 查看数据模型和 schemas

**访问方式**: 直接在浏览器中打开上述 URL

### 3. Swagger YAML 文件
**URL**: https://swagger-mock-api-five.vercel.app/swagger.yaml

**说明**: OpenAPI 3.0 规范的 YAML 格式文件

### 4. Swagger JSON 文件
**URL**: https://swagger-mock-api-five.vercel.app/swagger.json

**说明**: OpenAPI 3.0 规范的 JSON 格式文件

### 5. 健康检查端点
**URL**: https://swagger-mock-api-five.vercel.app/health

**说明**: 用于检查 API 服务器是否正常运行

## 快速访问

### 查看 API 文档（推荐）
👉 **直接访问**: https://swagger-mock-api-five.vercel.app/docs

### 验证 Request Demo Section
在 Swagger UI 中，您应该看到：
1. **Request Demo** section 出现在最前面
2. **Creator Profile** section 在 Request Demo 之后
3. `POST /api/v1/demo-requests` 端点可用

## 常见问题

### Q: 为什么根路径显示 JSON 而不是网页？
**A**: 这是正常的 API 服务器行为。根路径 `/` 返回 API 信息（JSON 格式）。要查看文档界面，请访问 `/docs`。

### Q: 如何查看完整的 API 文档？
**A**: 访问 https://swagger-mock-api-five.vercel.app/docs 查看交互式 Swagger UI。

### Q: 如何测试 API 端点？
**A**: 在 Swagger UI (`/docs`) 中，您可以：
1. 展开任意端点
2. 点击 "Try it out"
3. 填写参数
4. 点击 "Execute" 执行请求
5. 查看响应结果

### Q: Request Demo section 在哪里？
**A**: 在 Swagger UI 中，Request Demo 应该显示在文档的最顶部，在 Creator Profile 之前。

## API 端点列表

### Request Demo
- `POST /api/v1/demo-requests` - 提交演示请求

### Creator Profile
- `GET /creator/profile/me` - 获取创建者资料
- `PUT /creator/profile/me` - 更新创建者资料
- `POST /creator/profile/social-verification` - 社交账号验证

### 更多端点...
查看完整列表: https://swagger-mock-api-five.vercel.app/docs

## 部署状态

- **部署平台**: Vercel
- **项目名称**: swagger-mock-api
- **团队**: allweb3
- **GitHub 仓库**: https://github.com/Allweb3Labs/aw3-platform-mock-api

## 相关链接

- **Swagger UI**: https://swagger-mock-api-five.vercel.app/docs
- **Swagger YAML**: https://swagger-mock-api-five.vercel.app/swagger.yaml
- **健康检查**: https://swagger-mock-api-five.vercel.app/health
- **Vercel Dashboard**: https://vercel.com/allweb3/swagger-mock-api
