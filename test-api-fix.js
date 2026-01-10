/**
 * 测试 Request Demo API 是否正常工作
 */

const https = require('https');

const BASE_URL = 'swagger-mock-api-five.vercel.app';

function apiRequest(method, path, data = null) {
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

async function test() {
  console.log('\n╔════════════════════════════════════════════════════════════════╗');
  console.log('║           测试 Request Demo API 修复                            ║');
  console.log('╚════════════════════════════════════════════════════════════════╝\n');
  console.log(`Base URL: https://${BASE_URL}\n`);

  try {
    // 测试 Request Demo API
    console.log('📝 测试: POST /api/v1/demo-requests');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    
    const testData = {
      email: 'test@example.com',
      userType: 'creator',
      socialHandle: 'test_handle_' + Date.now(),
      socialPlatform: 'telegram',
      source: 'swagger_test'
    };
    
    console.log('\n请求数据:');
    console.log(JSON.stringify(testData, null, 2));
    
    const result = await apiRequest('POST', '/api/v1/demo-requests', testData);
    
    console.log('\n响应状态:', result.status);
    console.log('响应数据:');
    console.log(JSON.stringify(result.data, null, 2));
    
    if (result.status === 201 && result.data.success) {
      console.log('\n✅ 测试成功！');
      console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      console.log('\n🎉 API 端点现在工作正常！');
      console.log(`   Request ID: ${result.data.data.requestId}`);
      console.log(`   Email: ${result.data.data.email}`);
      console.log(`   User Type: ${result.data.data.userType}`);
      console.log(`   Status: ${result.data.data.status}`);
    } else if (result.status === 404) {
      console.log('\n❌ 仍然返回 404 错误');
      console.log('   可能需要等待 CDN 更新（1-2 分钟）');
    } else {
      console.log('\n⚠️  收到非预期响应');
      console.log('   状态码:', result.status);
    }
    
    console.log('\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('\n🌐 在 Swagger UI 中测试:');
    console.log('   1. 访问: https://swagger-mock-api-five.vercel.app/docs');
    console.log('   2. 按 Ctrl+Shift+R 强制刷新');
    console.log('   3. 展开 POST /api/v1/demo-requests');
    console.log('   4. 点击 "Try it out"');
    console.log('   5. 填写必填字段并点击 "Execute"');
    console.log('   6. 应该看到 201 Created 响应\n');
    
  } catch (error) {
    console.error('\n❌ 测试失败:', error.message);
    console.log('\n💡 提示: 这可能是网络连接问题');
    process.exit(1);
  }
}

test();
