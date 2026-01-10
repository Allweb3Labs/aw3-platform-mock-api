/**
 * 自动化 Vercel 部署脚本
 * 使用 Puppeteer 自动化 Chrome 完成部署
 */

const puppeteer = require('puppeteer');
const readline = require('readline');

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

function question(prompt) {
  return new Promise((resolve) => {
    rl.question(prompt, (answer) => {
      resolve(answer);
    });
  });
}

async function deployToVercel() {
  console.log('\n========================================');
  console.log('🚀 Vercel 自动化部署脚本');
  console.log('========================================\n');

  // 获取 GitHub 仓库信息
  const repoUrl = 'https://github.com/Allweb3Labs/aw3-platform-mock-api';
  console.log(`📦 GitHub 仓库: ${repoUrl}\n`);

  // 启动浏览器
  console.log('🌐 正在启动 Chrome 浏览器...');
  const browser = await puppeteer.launch({
    headless: false, // 显示浏览器窗口
    defaultViewport: null,
    args: [
      '--start-maximized',
      '--no-sandbox',
      '--disable-setuid-sandbox'
    ]
  });

  const page = await browser.newPage();
  
  try {
    // 步骤 1: 访问 Vercel
    console.log('\n📝 步骤 1/5: 访问 Vercel...');
    await page.goto('https://vercel.com/login', { waitUntil: 'networkidle2' });
    
    // 等待用户手动登录
    console.log('\n⚠️  请在浏览器中完成登录：');
    console.log('   - 使用 GitHub 登录（推荐）');
    console.log('   - 或使用邮箱登录');
    console.log('\n   登录完成后，按 Enter 继续...');
    await question('');

    // 步骤 2: 导航到新项目页面
    console.log('\n📝 步骤 2/5: 导航到导入项目页面...');
    await page.goto('https://vercel.com/new', { waitUntil: 'networkidle2' });
    await page.waitForTimeout(2000);

    // 步骤 3: 搜索并选择仓库
    console.log('\n📝 步骤 3/5: 搜索 GitHub 仓库...');
    
    // 尝试找到导入 Git 仓库的选项
    try {
      // 等待页面加载
      await page.waitForTimeout(3000);
      
      // 查找 "Import Git Repository" 或类似的输入框
      const searchInput = await page.$('input[placeholder*="Search"]') || 
                          await page.$('input[type="text"]');
      
      if (searchInput) {
        await searchInput.type('aw3-platform-mock-api');
        await page.waitForTimeout(2000);
      }
      
      console.log('   请在浏览器中选择仓库: Allweb3Labs/aw3-platform-mock-api');
      console.log('   选择完成后，按 Enter 继续...');
      await question('');
      
    } catch (e) {
      console.log('   请手动在浏览器中选择仓库');
      console.log('   选择完成后，按 Enter 继续...');
      await question('');
    }

    // 步骤 4: 配置项目
    console.log('\n📝 步骤 4/5: 配置项目...');
    console.log('   项目配置:');
    console.log('   - Framework Preset: Other');
    console.log('   - Build Command: (留空)');
    console.log('   - Install Command: npm install');
    console.log('\n   配置完成后，按 Enter 继续部署...');
    await question('');

    // 步骤 5: 点击部署
    console.log('\n📝 步骤 5/5: 开始部署...');
    
    // 尝试找到并点击 Deploy 按钮
    try {
      const deployButton = await page.$('button:has-text("Deploy")') ||
                           await page.$('button[type="submit"]');
      if (deployButton) {
        await deployButton.click();
        console.log('   ✓ 已点击部署按钮');
      } else {
        console.log('   请手动点击 Deploy 按钮');
      }
    } catch (e) {
      console.log('   请手动点击 Deploy 按钮');
    }

    // 等待部署完成
    console.log('\n⏳ 等待部署完成...');
    console.log('   部署过程通常需要 1-3 分钟');
    console.log('\n   部署完成后，按 Enter 获取部署 URL...');
    await question('');

    // 获取部署 URL
    const currentUrl = page.url();
    console.log('\n========================================');
    console.log('✅ 部署完成！');
    console.log('========================================');
    console.log(`\n当前页面: ${currentUrl}`);
    
    // 尝试获取部署 URL
    try {
      const deploymentUrl = await page.evaluate(() => {
        const links = document.querySelectorAll('a[href*=".vercel.app"]');
        return links.length > 0 ? links[0].href : null;
      });
      
      if (deploymentUrl) {
        console.log(`\n🌐 部署 URL: ${deploymentUrl}`);
        console.log(`\n测试 API:`);
        console.log(`   curl ${deploymentUrl}/health`);
        console.log(`   curl -X POST ${deploymentUrl}/api/v1/demo-requests \\`);
        console.log(`     -H "Content-Type: application/json" \\`);
        console.log(`     -d '{"email":"test@example.com","userType":"creator","socialHandle":"test","socialPlatform":"telegram"}'`);
      }
    } catch (e) {
      console.log('\n请从浏览器中复制部署 URL');
    }

    console.log('\n按 Enter 关闭浏览器...');
    await question('');

  } catch (error) {
    console.error('\n❌ 发生错误:', error.message);
  } finally {
    await browser.close();
    rl.close();
  }
}

// 运行
deployToVercel().catch(console.error);
