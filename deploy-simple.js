/**
 * 简化的 Vercel 部署脚本
 * 自动检测 Vercel CLI 的 token 或使用环境变量
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const os = require('os');

console.log('\n========================================');
console.log('🚀 Vercel 简化部署');
console.log('========================================\n');

// 尝试从 Vercel CLI 配置中读取 token
function getVercelToken() {
  // 检查环境变量
  if (process.env.VERCEL_TOKEN) {
    return process.env.VERCEL_TOKEN;
  }
  
  // 检查 Vercel CLI 配置文件
  const configPath = path.join(os.homedir(), '.vercel', 'auth.json');
  if (fs.existsSync(configPath)) {
    try {
      const config = JSON.parse(fs.readFileSync(configPath, 'utf-8'));
      if (config.token) {
        console.log('✓ 从 Vercel CLI 配置中找到 token\n');
        return config.token;
      }
    } catch (e) {}
  }
  
  return null;
}

async function deploy() {
  try {
    // 检查是否有 token
    const token = getVercelToken();
    
    if (!token) {
      console.log('⚠️  未找到 Vercel Token\n');
      console.log('请选择以下方式之一：\n');
      console.log('方式 1: 使用 Vercel CLI 登录');
      console.log('  运行: npx vercel login');
      console.log('  然后重新运行此脚本\n');
      console.log('方式 2: 手动设置 Token');
      console.log('  1. 访问: https://vercel.com/account/tokens');
      console.log('  2. 创建新 Token');
      console.log('  3. 运行: $env:VERCEL_TOKEN="your_token"');
      console.log('  4. 重新运行此脚本\n');
      console.log('方式 3: 通过 Vercel 网站部署');
      console.log('  访问: https://vercel.com/new');
      console.log('  导入: Allweb3Labs/aw3-platform-mock-api\n');
      process.exit(1);
    }

    // 设置环境变量
    process.env.VERCEL_TOKEN = token;
    
    console.log('📝 开始部署...\n');
    
    // 使用 vercel CLI 部署
    console.log('正在执行: vercel --prod --yes --token ...\n');
    
    const result = execSync('npx vercel --prod --yes --confirm', {
      env: { ...process.env, VERCEL_TOKEN: token },
      stdio: 'inherit',
      cwd: __dirname
    });
    
    console.log('\n========================================');
    console.log('✅ 部署完成！');
    console.log('========================================\n');
    
  } catch (error) {
    console.error('\n❌ 部署失败:', error.message);
    process.exit(1);
  }
}

deploy();
