// 修复版本的 server.js - 添加更好的错误处理
// 如果原 server.js 有问题，可以临时使用这个版本测试

const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// 错误处理中间件
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    success: false,
    error: {
      code: 'INTERNAL_ERROR',
      message: err.message || 'Internal server error'
    },
    timestamp: new Date().toISOString()
  });
});

// 健康检查端点（简单测试）
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    environment: process.env.VERCEL ? 'vercel' : 'local',
    cwd: process.cwd(),
    __dirname: __dirname
  });
});

// 根路径
app.get('/', (req, res) => {
  res.json({
    name: "AW3 Platform Mock API",
    version: "1.0.0",
    documentation: "/docs",
    status: "running",
    endpoints: {
      swagger: {
        yaml: "/swagger.yaml",
        json: "/swagger.json",
        ui: "/docs"
      },
      health: "/health"
    }
  });
});

// 尝试加载 Swagger（带错误处理）
let swaggerDocument = null;
try {
  const swaggerUi = require('swagger-ui-express');
  const YAML = require('yamljs');
  
  const swaggerPath = path.join(__dirname, 'swagger.yaml');
  console.log('Attempting to load swagger.yaml from:', swaggerPath);
  console.log('File exists:', fs.existsSync(swaggerPath));
  
  if (fs.existsSync(swaggerPath)) {
    swaggerDocument = YAML.load(swaggerPath);
    console.log('Swagger document loaded successfully');
    
    // Serve Swagger UI
    const swaggerOptions = {
      customCss: '.swagger-ui .topbar { display: none }',
      customSiteTitle: "AW3 Platform API Documentation",
      customCssUrl: 'https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.9.0/swagger-ui.min.css',
      customJs: [
        'https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.9.0/swagger-ui-bundle.min.js',
        'https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.9.0/swagger-ui-standalone-preset.min.js'
      ]
    };
    
    app.use('/docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument, swaggerOptions));
    
    // Serve raw swagger files
    app.get('/swagger.yaml', (req, res) => {
      res.sendFile(swaggerPath);
    });
    
    app.get('/swagger.json', (req, res) => {
      res.json(swaggerDocument);
    });
  } else {
    console.error('swagger.yaml not found at:', swaggerPath);
    app.get('/docs', (req, res) => {
      res.status(500).json({
        error: 'Swagger documentation not available',
        path: swaggerPath,
        cwd: process.cwd(),
        __dirname: __dirname
      });
    });
  }
} catch (error) {
  console.error('Failed to load Swagger:', error);
  app.get('/docs', (req, res) => {
    res.status(500).json({
      error: 'Failed to load Swagger documentation',
      message: error.message
    });
  });
}

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: {
      code: 'NOT_FOUND',
      message: 'Endpoint not found'
    },
    timestamp: new Date().toISOString()
  });
});

// Start server (for local development)
if (process.env.VERCEL !== '1') {
  app.listen(PORT, () => {
    console.log(`AW3 Platform Mock API running on port ${PORT}`);
    console.log(`Swagger UI: http://localhost:${PORT}/docs`);
    console.log(`Health check: http://localhost:${PORT}/health`);
  });
}

// Export for Vercel serverless
module.exports = app;
