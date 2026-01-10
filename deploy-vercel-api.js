/**
 * 使用 Vercel API 自动部署
 * 无需浏览器，直接通过 API 部署
 */

const https = require('https');
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// 配置
const GITHUB_REPO = 'Allweb3Labs/aw3-platform-mock-api';
const PROJECT_NAME = 'aw3-platform-mock-api';

console.log('\n========================================');
console.log('🚀 Vercel API 自动化部署');
console.log('========================================\n');

// 检查 Vercel Token
let VERCEL_TOKEN = process.env.VERCEL_TOKEN;

if (!VERCEL_TOKEN) {
  console.log('⚠️  未找到 VERCEL_TOKEN 环境变量');
  console.log('\n请按以下步骤获取 Vercel Token:');
  console.log('1. 访问: https://vercel.com/account/tokens');
  console.log('2. 点击 "Create Token"');
  console.log('3. 设置 Token 名称（如: auto-deploy）');
  console.log('4. 选择 Scope: Full Account');
  console.log('5. 复制生成的 Token');
  console.log('\n然后运行:');
  console.log('$env:VERCEL_TOKEN="your_token_here"');
  console.log('node deploy-vercel-api.js');
  process.exit(1);
}

// API 辅助函数
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

async function deployToVercel() {
  try {
    // 步骤 1: 验证 Token
    console.log('📝 步骤 1/4: 验证 Vercel Token...');
    const user = await apiRequest('GET', '/v2/user');
    console.log(`   ✓ 已登录为: ${user.username || user.email}`);

    // 步骤 2: 检查或创建项目
    console.log('\n📝 步骤 2/4: 检查项目...');
    let project;
    try {
      project = await apiRequest('GET', `/v9/projects/${PROJECT_NAME}`);
      console.log(`   ✓ 找到项目: ${project.name}`);
    } catch (e) {
      console.log('   项目不存在，正在创建...');
      
      // 创建新项目
      project = await apiRequest('POST', '/v9/projects', {
        name: PROJECT_NAME,
        framework: 'other',
        gitRepository: {
          type: 'github',
          repo: GITHUB_REPO
        }
      });
      console.log(`   ✓ 已创建项目: ${project.name}`);
    }

    // 步骤 3: 触发部署
    console.log('\n📝 步骤 3/4: 触发部署...');
    
    // 获取最新的 commit
    console.log('   获取最新 commit...');
    const commitHash = execSync('git rev-parse HEAD').toString().trim();
    const commitMsg = execSync('git log -1 --pretty=%B').toString().trim();
    console.log(`   Commit: ${commitHash.substring(0, 8)} - ${commitMsg}`);

    // 创建部署
    const deployment = await apiRequest('POST', '/v13/deployments', {
      name: PROJECT_NAME,
      gitSource: {
        type: 'github',
        repo: GITHUB_REPO,
        ref: 'main'
      },
      projectSettings: {
        framework: null,
        buildCommand: null,
        installCommand: 'npm install',
        outputDirectory: null
      },
      target: 'production'
    });

    console.log(`   ✓ 部署已触发`);
    console.log(`   部署 ID: ${deployment.id}`);
    console.log(`   状态: ${deployment.readyState}`);

    // 步骤 4: 等待部署完成
    console.log('\n📝 步骤 4/4: 等待部署完成...');
    console.log('   这可能需要 1-3 分钟...');

    let deploymentStatus = deployment;
    let attempts = 0;
    const maxAttempts = 60; // 5 分钟超时

    while (attempts < maxAttempts) {
      await new Promise(resolve => setTimeout(resolve, 5000)); // 每 5 秒检查一次
      
      try {
        deploymentStatus = await apiRequest('GET', `/v13/deployments/${deployment.id}`);
        
        if (deploymentStatus.readyState === 'READY') {
          console.log(`   ✓ 部署完成！`);
          break;
        } else if (deploymentStatus.readyState === 'ERROR') {
          throw new Error('部署失败: ' + (deploymentStatus.error?.message || '未知错误'));
        }
        
        // 显示进度
        if (attempts % 2 === 0) { // 每 10 秒显示一次
          console.log(`   ... 部署中 (${deploymentStatus.readyState}) - ${attempts * 5}秒`);
        }
      } catch (e) {
        console.log(`   检查状态失败: ${e.message}`);
      }
      
      attempts++;
    }

    if (deploymentStatus.readyState !== 'READY') {
      throw new Error('部署超时');
    }

    // 输出结果
    console.log('\n========================================');
    console.log('✅ 部署成功！');
    console.log('========================================');
    
    const deploymentUrl = `https://${deploymentStatus.url}`;
    console.log(`\n🌐 部署 URL: ${deploymentUrl}`);
    console.log(`\n📋 API 端点:`);
    console.log(`   - 健康检查: ${deploymentUrl}/health`);
    console.log(`   - Swagger UI: ${deploymentUrl}/docs`);
    console.log(`   - Request Demo: POST ${deploymentUrl}/api/v1/demo-requests`);
    
    console.log(`\n🧪 测试命令:`);
    console.log(`   curl ${deploymentUrl}/health`);
    console.log(`\n   curl -X POST ${deploymentUrl}/api/v1/demo-requests \\`);
    console.log(`     -H "Content-Type: application/json" \\`);
    console.log(`     -d '{"email":"test@example.com","userType":"creator","socialHandle":"test","socialPlatform":"telegram"}'`);
    
    console.log(`\n📊 Vercel Dashboard:`);
    console.log(`   https://vercel.com/${user.username}/${PROJECT_NAME}`);

  } catch (error) {
    console.error('\n❌ 部署失败:', error.message);
    process.exit(1);
  }
}

// 运行部署
deployToVercel();
