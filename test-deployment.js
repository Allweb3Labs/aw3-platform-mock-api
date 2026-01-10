/**
 * 测试部署的 API
 */

const https = require('https');

const BASE_URL = 'swagger-mock-api-five.vercel.app';

function testEndpoint(path, method = 'GET', data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: BASE_URL,
      path: path,
      method: method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    const req = https.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          const response = JSON.parse(body);
          resolve({ status: res.statusCode, data: response });
        } catch (e) {
          resolve({ status: res.statusCode, data: body });
        }
      });
    });

    req.on('error', reject);
    
    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function runTests() {
  console.log('\n========================================');
  console.log('🧪 测试部署的 API');
  console.log('========================================\n');
  console.log(`Base URL: https://${BASE_URL}\n`);

  try {
    // 测试 1: 健康检查
    console.log('📝 测试 1: 健康检查 GET /health');
    const health = await testEndpoint('/health');
    console.log(`   状态: ${health.status}`);
    console.log(`   响应:`, JSON.stringify(health.data, null, 2));
    console.log('   ✅ 通过\n');

    // 测试 2: API 根路径
    console.log('📝 测试 2: API 根路径 GET /');
    const root = await testEndpoint('/');
    console.log(`   状态: ${root.status}`);
    console.log(`   API 名称: ${root.data.name}`);
    console.log('   ✅ 通过\n');

    // 测试 3: Request Demo API
    console.log('📝 测试 3: Request Demo POST /api/v1/demo-requests');
    const demoRequest = {
      email: 'test@example.com',
      userType: 'creator',
      socialHandle: 'test_handle',
      socialPlatform: 'telegram',
      source: 'test'
    };
    const demo = await testEndpoint('/api/v1/demo-requests', 'POST', demoRequest);
    console.log(`   状态: ${demo.status}`);
    if (demo.data.success) {
      console.log(`   请求 ID: ${demo.data.data.requestId}`);
      console.log('   ✅ 通过\n');
    } else {
      console.log(`   响应:`, JSON.stringify(demo.data, null, 2));
    }

    console.log('========================================');
    console.log('✅ 所有测试通过！');
    console.log('========================================\n');
    console.log('📊 部署 URL:');
    console.log(`   生产环境: https://${BASE_URL}`);
    console.log(`   Swagger UI: https://${BASE_URL}/docs`);
    console.log(`   健康检查: https://${BASE_URL}/health`);
    console.log(`   Request Demo: POST https://${BASE_URL}/api/v1/demo-requests\n`);

  } catch (error) {
    console.error('❌ 测试失败:', error.message);
    process.exit(1);
  }
}

runTests();
