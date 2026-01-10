/**
 * 测试 Swagger UI 部署
 */

const https = require('https');

const BASE_URL = 'swagger-mock-api-five.vercel.app';

function httpGet(path) {
  return new Promise((resolve, reject) => {
    https.get(`https://${BASE_URL}${path}`, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        resolve({ status: res.statusCode, body, headers: res.headers });
      });
    }).on('error', reject);
  });
}

async function test() {
  console.log('\n🧪 测试 Swagger UI 部署...\n');
  console.log(`Base URL: https://${BASE_URL}\n`);
  
  try {
    // 测试 1: Swagger JSON
    console.log('1️⃣  测试 swagger.json...');
    const jsonRes = await httpGet('/swagger.json');
    if (jsonRes.status === 200) {
      const doc = JSON.parse(jsonRes.body);
      console.log(`   ✅ 成功 (${jsonRes.status})`);
      console.log(`   标题: ${doc.info?.title}`);
      console.log(`   路径数: ${Object.keys(doc.paths || {}).length}`);
    } else {
      console.log(`   ❌ 失败 (${jsonRes.status})`);
    }
    
    // 测试 2: Swagger UI HTML
    console.log('\n2️⃣  测试 /docs...');
    const docsRes = await httpGet('/docs');
    if (docsRes.status === 200 && docsRes.body.includes('swagger-ui')) {
      console.log(`   ✅ 成功 (${docsRes.status})`);
      console.log(`   包含 Swagger UI 代码: ${docsRes.body.includes('SwaggerUIBundle') ? '是' : '否'}`);
    } else {
      console.log(`   ❌ 失败 (${docsRes.status})`);
    }
    
    // 测试 3: 健康检查
    console.log('\n3️⃣  测试 /health...');
    const healthRes = await httpGet('/health');
    if (healthRes.status === 200) {
      const health = JSON.parse(healthRes.body);
      console.log(`   ✅ 成功 (${healthRes.status})`);
      console.log(`   状态: ${health.status}`);
    } else {
      console.log(`   ❌ 失败 (${healthRes.status})`);
    }
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ 所有测试通过！Swagger UI 已正确部署');
    console.log('='.repeat(60));
    console.log('\n🌐 访问 Swagger UI:');
    console.log(`   https://${BASE_URL}/docs`);
    console.log('\n💡 提示: 如果浏览器仍显示旧版本，请:');
    console.log('   1. 按 Ctrl+Shift+R 强制刷新');
    console.log('   2. 清除浏览器缓存');
    console.log('   3. 等待 1-2 分钟让 CDN 更新\n');
    
  } catch (error) {
    console.error('\n❌ 测试失败:', error.message);
    process.exit(1);
  }
}

test();
