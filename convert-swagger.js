/**
 * 将 swagger.yaml 转换为 swagger.json
 * 用于 Vercel 部署时避免文件系统读取问题
 */

const fs = require('fs');
const path = require('path');

try {
  console.log('Converting swagger.yaml to swagger.json...');
  
  const swaggerPath = path.join(__dirname, 'swagger.yaml');
  
  // 读取 YAML 文件内容
  const yamlContent = fs.readFileSync(swaggerPath, 'utf8');
  
  // 使用 yamljs 解析（延迟加载以便错误处理）
  const YAML = require('yamljs');
  
  // 尝试解析 YAML
  let swaggerDocument;
  try {
    swaggerDocument = YAML.parse(yamlContent);
  } catch (parseError) {
    console.error('YAML parse error:', parseError.message);
    console.log('Trying alternative method...');
    swaggerDocument = YAML.load(swaggerPath);
  }
  
  if (!swaggerDocument || !swaggerDocument.paths) {
    throw new Error('Invalid swagger document: missing paths');
  }
  
  const jsonPath = path.join(__dirname, 'swagger.json');
  fs.writeFileSync(jsonPath, JSON.stringify(swaggerDocument, null, 2));
  
  console.log('✅ Successfully created swagger.json');
  console.log(`   Paths: ${Object.keys(swaggerDocument.paths || {}).length}`);
  console.log(`   Size: ${(fs.statSync(jsonPath).size / 1024).toFixed(2)} KB`);
} catch (error) {
  console.error('❌ Error converting swagger.yaml:', error.message);
  console.error('Stack:', error.stack);
  process.exit(1);
}
