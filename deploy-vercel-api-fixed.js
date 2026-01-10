/**
 * 使用 Vercel API 自动部署 - 修复版
 */

const https = require('https');
const { execSync } = require('child_process');

const GITHUB_REPO = 'Allweb3Labs/aw3-platform-mock-api';
const PROJECT_NAME = 'aw3-platform-mock-api';
const VERCEL_TOKEN = process.env.VERCEL_TOKEN;

console.log('\n========================================');
console.log('🚀 Vercel API 自动化部署');
console.log('========================================\n');

if (!VERCEL_TOKEN) {
  console.log('❌ 未找到 VERCEL_TOKEN');
  process.exit(1);
}

function apiRequest(method, path, data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'api.vercel.com',
      path: path,
      method: method,
      headers: {
        'Authorization': `Bearer ${VERCEL_TOKEN}`,
        'Content-Type': 'application/json'
      }
    };

    const req = https.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          const response = JSON.parse(body);
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(response);
          } else {
            reject(new Error(`API Error: ${res.statusCode} - ${JSON.stringify(response)}`));
          }
        } catch (e) {
          reject(new Error(`Parse Error: ${body}`));
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

async function deploy() {
  try {
    // 验证 Token
    console.log('📝 步骤 1/3: 验证 Token...');
    const user = await apiRequest('GET', '/v2/user');
    console.log(`   ✓ 已登录为: ${user.username || user.email || user.name}`);

    // 获取或创建项目
    console.log('\n📝 步骤 2/3: 检查项目...');
    let project;
    try {
      project = await apiRequest('GET', `/v9/projects/${PROJECT_NAME}`);
      console.log(`   ✓ 找到项目: ${project.name}`);
      console.log(`   项目 ID: ${project.id}`);
    } catch (e) {
      console.log('   项目不存在，正在创建...');
      project = await apiRequest('POST', '/v9/projects', {
        name: PROJECT_NAME,
        framework: null
      });
      console.log(`   ✓ 已创建项目: ${project.name}`);
    }

    // 使用 Vercel CLI 部署（更可靠）
    console.log('\n📝 步骤 3/3: 部署项目...');
    console.log('   使用 Vercel CLI 部署...\n');
    
    try {
      // 设置环境变量并运行 vercel 命令
      process.env.VERCEL_TOKEN = VERCEL_TOKEN;
      process.env.VERCEL_ORG_ID = user.id;
      process.env.VERCEL_PROJECT_ID = project.id;
      
      const deployCmd = 'npx vercel --prod --yes --token ' + VERCEL_TOKEN;
      const result = execSync(deployCmd, {
        stdio: 'inherit',
        cwd: __dirname
      });
      
      console.log('\n========================================');
      console.log('✅ 部署成功！');
      console.log('========================================');
      
      // 获取最新部署
      const deployments = await apiRequest('GET', `/v6/deployments?projectId=${project.id}&limit=1`);
      if (deployments.deployments && deployments.deployments.length > 0) {
        const deployment = deployments.deployments[0];
        const deploymentUrl = `https://${deployment.url}`;
        
        console.log(`\n🌐 部署 URL: ${deploymentUrl}`);
        console.log(`\n📋 API 端点:`);
        console.log(`   - 健康检查: ${deploymentUrl}/health`);
        console.log(`   - Swagger UI: ${deploymentUrl}/docs`);
        console.log(`   - Request Demo: POST ${deploymentUrl}/api/v1/demo-requests`);
        
        console.log(`\n🧪 测试命令:`);
        console.log(`   curl ${deploymentUrl}/health\n`);
      }
      
    } catch (cliError) {
      console.log('\n使用 CLI 部署失败，尝试直接通过 API...\n');
      
      // 如果 CLI 失败，尝试直接通过 API（需要 GitHub 集成）
      console.log('请访问以下 URL 手动完成部署:');
      console.log(`https://vercel.com/${user.username}/${PROJECT_NAME}/settings/git`);
      console.log('\n连接 GitHub 仓库后，将自动部署。');
    }

  } catch (error) {
    console.error('\n❌ 部署失败:', error.message);
    process.exit(1);
  }
}

deploy();
