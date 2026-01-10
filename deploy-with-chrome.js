/**
 * 使用 Chrome Canary 自动化 Vercel 部署
 * 修复序列化错误
 */

const puppeteer = require('puppeteer-core');

const CHROME_CANARY_PATH = 'C:\\Users\\12549\\AppData\\Local\\Google\\Chrome SxS\\Application\\chrome.exe';
const GITHUB_REPO = 'Allweb3Labs/aw3-platform-mock-api';

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function deployToVercel() {
  console.log('\n========================================');
  console.log('🚀 Vercel 自动化部署 (Chrome Canary)');
  console.log('========================================\n');

  console.log('🌐 启动 Chrome Canary...');
  
  const browser = await puppeteer.launch({
    executablePath: CHROME_CANARY_PATH,
    headless: false,
    defaultViewport: null,
    args: [
      '--start-maximized',
      '--no-sandbox',
      '--disable-setuid-sandbox',
      '--disable-blink-features=AutomationControlled'
    ],
    ignoreDefaultArgs: ['--enable-automation']
  });

  const page = await browser.newPage();
  
  // 设置 User Agent
  await page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');

  try {
    // 步骤 1: 访问 Vercel 登录
    console.log('\n📝 步骤 1: 访问 Vercel 登录页面...');
    await page.goto('https://vercel.com/login', { 
      waitUntil: 'networkidle2',
      timeout: 60000 
    });
    
    console.log('   ✓ 页面已加载');
    console.log('\n⏳ 等待登录完成...');
    console.log('   请在浏览器中使用 GitHub 或邮箱登录 Vercel');
    
    // 等待登录完成 - 检测 URL 变化
    await page.waitForFunction(
      () => !window.location.href.includes('/login'),
      { timeout: 300000 } // 5 分钟超时
    );
    
    console.log('   ✓ 登录成功！');
    await sleep(2000);

    // 步骤 2: 导航到新项目页面
    console.log('\n📝 步骤 2: 导航到导入项目页面...');
    await page.goto('https://vercel.com/new', { 
      waitUntil: 'networkidle2',
      timeout: 60000 
    });
    await sleep(3000);
    console.log('   ✓ 已进入新项目页面');

    // 步骤 3: 查找并点击 Import 按钮
    console.log('\n📝 步骤 3: 查找 GitHub 仓库...');
    
    // 等待页面完全加载
    await sleep(2000);
    
    // 尝试查找搜索框或仓库列表
    try {
      // 查找搜索输入框
      const searchSelectors = [
        'input[placeholder*="Search"]',
        'input[placeholder*="search"]',
        'input[type="search"]',
        'input[name="search"]'
      ];
      
      let searchInput = null;
      for (const selector of searchSelectors) {
        try {
          searchInput = await page.$(selector);
          if (searchInput) {
            console.log(`   找到搜索框: ${selector}`);
            break;
          }
        } catch (e) {}
      }
      
      if (searchInput) {
        await searchInput.click();
        await searchInput.type('aw3-platform-mock-api', { delay: 50 });
        console.log('   ✓ 已输入仓库名称');
        await sleep(2000);
      }
      
      // 尝试点击 Import 按钮（使用 evaluate 避免序列化问题）
      const clicked = await page.evaluate((repoName) => {
        // 查找包含仓库名的元素
        const elements = document.querySelectorAll('*');
        for (const el of elements) {
          if (el.textContent && el.textContent.includes(repoName)) {
            // 查找附近的 Import 按钮
            const parent = el.closest('div');
            if (parent) {
              const buttons = parent.querySelectorAll('button');
              for (const btn of buttons) {
                if (btn.textContent.includes('Import')) {
                  btn.click();
                  return true;
                }
              }
            }
          }
        }
        return false;
      }, GITHUB_REPO);
      
      if (clicked) {
        console.log('   ✓ 已点击 Import 按钮');
      } else {
        console.log('   请在浏览器中手动选择仓库并点击 Import');
      }
      
    } catch (e) {
      console.log('   请在浏览器中手动选择仓库: ' + GITHUB_REPO);
    }

    // 等待进入配置页面
    console.log('\n⏳ 等待进入项目配置页面...');
    await sleep(5000);
    
    // 步骤 4: 配置项目并部署
    console.log('\n📝 步骤 4: 配置并部署...');
    
    // 尝试找到并点击 Deploy 按钮（使用 evaluate 避免序列化问题）
    let deployed = false;
    for (let i = 0; i < 30; i++) {
      try {
        // 使用 evaluate 在浏览器上下文中点击
        const clicked = await page.evaluate(() => {
          const buttons = document.querySelectorAll('button');
          for (const btn of buttons) {
            if (btn.textContent.includes('Deploy') && !btn.disabled) {
              btn.click();
              return true;
            }
          }
          return false;
        });
        
        if (clicked) {
          console.log('   ✓ 已点击 Deploy 按钮');
          deployed = true;
          break;
        }
      } catch (e) {}
      
      await sleep(1000);
      
      // 检查是否已经在部署页面
      const url = page.url();
      if (url.includes('/deployments/') || url.includes('congratulations')) {
        deployed = true;
        console.log('   ✓ 检测到部署已开始');
        break;
      }
    }

    if (!deployed) {
      console.log('\n   ⚠️  未能自动点击 Deploy 按钮');
      console.log('   请在浏览器中手动点击 Deploy 按钮');
      console.log('   等待手动操作...');
      
      // 等待用户手动部署
      await page.waitForFunction(
        () => window.location.href.includes('/deployments/') || 
              window.location.href.includes('congratulations'),
        { timeout: 300000 }
      );
      console.log('   ✓ 检测到部署已开始');
    }

    // 步骤 5: 等待部署完成
    console.log('\n📝 步骤 5: 等待部署完成...');
    console.log('   这可能需要 1-3 分钟...');
    
    // 等待部署完成
    let deploymentUrl = null;
    for (let i = 0; i < 180; i++) { // 最多等待 3 分钟
      await sleep(1000);
      
      const url = page.url();
      
      // 检查是否部署成功
      if (url.includes('congratulations') || url.includes('/deployments/')) {
        // 尝试获取部署 URL（避免序列化问题）
        try {
          deploymentUrl = await page.evaluate(() => {
            const links = document.querySelectorAll('a');
            for (const link of links) {
              if (link.href && link.href.includes('.vercel.app') && !link.href.includes('vercel.com')) {
                return link.href;
              }
            }
            // 尝试从文本中获取
            const text = document.body.innerText;
            const match = text.match(/https?:\/\/[a-z0-9-]+\.vercel\.app/i);
            return match ? match[0] : null;
          });
          
          if (deploymentUrl) {
            console.log(`\n   ✓ 部署完成！`);
            break;
          }
        } catch (e) {}
      }
      
      // 每 10 秒显示进度
      if (i > 0 && i % 10 === 0) {
        console.log(`   ... 已等待 ${i} 秒`);
      }
    }

    // 如果还没有获取到 URL，再尝试一次
    if (!deploymentUrl) {
      try {
        deploymentUrl = await page.evaluate(() => {
          const text = document.body.innerText;
          const match = text.match(/https?:\/\/[a-z0-9-]+\.vercel\.app/i);
          return match ? match[0] : null;
        });
      } catch (e) {}
    }

    // 输出结果
    console.log('\n========================================');
    console.log('✅ 部署流程完成！');
    console.log('========================================');
    
    if (deploymentUrl) {
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
    } else {
      const currentUrl = page.url();
      console.log(`\n当前页面: ${currentUrl}`);
      console.log('\n请从浏览器中复制部署 URL');
    }

    // 保持浏览器打开一段时间
    console.log('\n浏览器将在 30 秒后关闭...');
    console.log('你可以在浏览器中查看部署详情');
    await sleep(30000);

  } catch (error) {
    console.error('\n❌ 发生错误:', error.message);
    console.log('\n浏览器将保持打开状态 60 秒，请手动完成部署');
    await sleep(60000);
  } finally {
    await browser.close();
    console.log('\n✓ 浏览器已关闭');
  }
}

// 运行
console.log('正在启动自动化部署...');
deployToVercel().catch(err => {
  console.error('部署失败:', err);
  process.exit(1);
});
