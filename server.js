const express = require('express');
const cors = require('cors');
const swaggerUi = require('swagger-ui-express');
const YAML = require('yamljs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs').promises;

const app = express();
const PORT = process.env.PORT || 3000;

// In-memory store for demo requests (persists during warm function instances)
// This is necessary because Vercel's serverless environment has a read-only file system
const inMemoryDemoRequests = [];

// Middleware
app.use(cors());
app.use(express.json());

// Load Swagger document with error handling
let swaggerDocument;
try {
  const fs = require('fs');
  
  // First, try to load from swagger.json (pre-built for Vercel)
  const jsonPath = path.join(__dirname, 'swagger.json');
  if (fs.existsSync(jsonPath)) {
    console.log('Loading swagger.json from:', jsonPath);
    const jsonContent = fs.readFileSync(jsonPath, 'utf8');
    swaggerDocument = JSON.parse(jsonContent);
    console.log('✅ Swagger document loaded from JSON');
    console.log('   Number of paths:', Object.keys(swaggerDocument.paths || {}).length);
  } else {
    // Fallback to YAML
    console.log('swagger.json not found, trying YAML...');
    let swaggerPath = path.join(__dirname, 'swagger.yaml');
    
    if (!fs.existsSync(swaggerPath)) {
      swaggerPath = path.join(process.cwd(), 'swagger.yaml');
      if (!fs.existsSync(swaggerPath)) {
        swaggerPath = './swagger.yaml';
      }
    }
    
    console.log('Loading swagger.yaml from:', swaggerPath);
    swaggerDocument = YAML.load(swaggerPath);
    console.log('✅ Swagger document loaded from YAML');
    console.log('   Number of paths:', Object.keys(swaggerDocument.paths || {}).length);
  }
  
  // Validate loaded document
  if (!swaggerDocument || !swaggerDocument.paths || Object.keys(swaggerDocument.paths).length === 0) {
    throw new Error('Swagger document is empty or invalid');
  }
} catch (error) {
  console.error('❌ Failed to load swagger document:', error.message);
  console.error('Error stack:', error.stack);
  // Use a minimal fallback document
  swaggerDocument = {
    openapi: '3.0.0',
    info: { title: 'AW3 Platform Mock API', version: '1.0.0' },
    servers: [
      { url: 'https://swagger-mock-api-five.vercel.app', description: 'Production' }
    ],
    paths: {}
  };
}

// Serve Swagger UI with CDN assets for serverless compatibility
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
  res.sendFile(path.join(__dirname, 'swagger.yaml'));
});

app.get('/swagger.json', (req, res) => {
  res.json(swaggerDocument);
});

// Helper to wrap responses
const apiResponse = (data) => ({
  success: true,
  data,
  timestamp: new Date().toISOString()
});

const apiError = (code, message) => ({
  success: false,
  error: { code, message },
  timestamp: new Date().toISOString()
});

// Enum mappings
const ENUMS = {
  UserRole: {
    1: 'Creator',
    2: 'Projector',
    3: 'Admin',
    4: 'Validator'
  },
  WalletType: {
    1: 'MetaMask',
    2: 'WalletConnect',
    3: 'Coinbase',
    4: 'Rainbow'
  },
  SocialPlatform: {
    1: 'Twitter',
    2: 'YouTube',
    3: 'Instagram',
    4: 'TikTok',
    5: 'Discord'
  },
  FocusArea: {
    1: 'DeFi',
    2: 'NFT',
    3: 'Gaming',
    4: 'Infrastructure',
    5: 'L2',
    6: 'DAO',
    7: 'Metaverse',
    8: 'Trading',
    9: 'Other'
  },
  CampaignStatus: {
    1: 'DRAFT',
    2: 'PENDING_ESCROW',
    3: 'ACTIVE',
    4: 'IN_PROGRESS',
    5: 'COMPLETED',
    6: 'CANCELLED',
    7: 'SUSPENDED'
  },
  ApplicationStatus: {
    1: 'PENDING',
    2: 'ACCEPTED',
    3: 'REJECTED',
    4: 'WITHDRAWN'
  },
  DeliverableStatus: {
    1: 'SUBMITTED',
    2: 'PENDING_VERIFICATION',
    3: 'VERIFIED',
    4: 'REJECTED',
    5: 'PAID'
  },
  DeliverableType: {
    1: 'Twitter Posts',
    2: 'Videos',
    3: 'Articles',
    4: 'AMAs',
    5: 'Discord Management',
    6: 'Community Growth',
    7: 'Instagram Post',
    8: 'TikTok'
  },
  CampaignDuration: {
    1: 'Less Than 1 Week',
    2: '1-2 Weeks',
    3: '2-4 Weeks',
    4: '1-3 Months',
    5: '3+ Months'
  },
  Complexity: {
    1: 'LOW',
    2: 'MEDIUM',
    3: 'HIGH'
  },
  PaymentToken: {
    1: 'USDC',
    2: 'USDT',
    3: 'ETH',
    4: 'AW3'
  },
  TimePeriod: {
    1: '7d',
    2: '30d',
    3: '90d',
    4: '1y'
  },
  EarningsRange: {
    1: 'Hourly',
    2: 'Daily',
    3: 'Monthly'
  }
};

// Match rate calculation helper
const calculateMatchRate = (creator, campaign) => {
  if (!creator || !campaign || !campaign.requiredReputation || !campaign.kpiTargets) {
    return null;
  }
  
  const reputationMatch = creator.reputation >= campaign.requiredReputation ? 1 : 0.5;
  const focusAreaMatch = creator.focusArea?.some(fa => campaign.focusArea === fa) ? 1 : 0.6;
  
  const matchRate = (reputationMatch * 0.6 + focusAreaMatch * 0.4) * 100;
  return Math.round(matchRate * 10) / 10;
};

// ============ REQUEST DEMO API HELPERS ============
const DEMO_REQUESTS_FILE = path.join(__dirname, 'demo-requests.txt');

// Rate limiting storage (in-memory)
const rateLimitStore = {
  ip: {},
  email: {}
};

// Clean up old rate limit entries (run every hour)
setInterval(() => {
  const now = Date.now();
  const oneDay = 24 * 60 * 60 * 1000;

  // Clean IP entries older than 1 day
  for (const ip in rateLimitStore.ip) {
    rateLimitStore.ip[ip].requests = rateLimitStore.ip[ip].requests.filter(
      time => now - time < oneDay
    );
    if (rateLimitStore.ip[ip].requests.length === 0) {
      delete rateLimitStore.ip[ip];
    }
  }

  // Clean email entries older than 1 day
  for (const email in rateLimitStore.email) {
    rateLimitStore.email[email].requests = rateLimitStore.email[email].requests.filter(
      time => now - time < oneDay
    );
    if (rateLimitStore.email[email].requests.length === 0) {
      delete rateLimitStore.email[email];
    }
  }
}, 60 * 60 * 1000); // Run every hour

// Validation functions
const validateEmail = (email) => {
  if (!email || typeof email !== 'string') return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) return false;
  if (email.length > 255) return false;
  return true;
};

const validateSocialHandle = (handle) => {
  if (!handle || typeof handle !== 'string') return false;
  const cleaned = stripAtSymbol(handle);
  if (cleaned.length < 3 || cleaned.length > 50) return false;
  const handleRegex = /^[a-zA-Z0-9_-]+$/;
  if (!handleRegex.test(cleaned)) return false;
  if (cleaned.startsWith('-') || cleaned.startsWith('_') || 
      cleaned.endsWith('-') || cleaned.endsWith('_')) return false;
  return true;
};

const normalizeEmail = (email) => {
  return email ? email.toLowerCase().trim() : '';
};

const normalizeUserType = (type) => {
  if (!type || typeof type !== 'string') return '';
  const normalized = type.toLowerCase().trim();
  return (normalized === 'creator' || normalized === 'project_owner') ? normalized : '';
};

const normalizePlatform = (platform) => {
  if (!platform || typeof platform !== 'string') return '';
  const normalized = platform.toLowerCase().trim();
  return (normalized === 'telegram' || normalized === 'x') ? normalized : '';
};

const stripAtSymbol = (handle) => {
  if (!handle || typeof handle !== 'string') return '';
  return handle.replace(/^@+/, '').trim();
};

const generateRequestId = () => {
  return 'req_' + uuidv4().replace(/-/g, '').substring(0, 12);
};

// File operations
const readDemoRequests = async () => {
  let fileRequests = [];
  
  // Try to read from file first
  try {
    const content = await fs.readFile(DEMO_REQUESTS_FILE, 'utf-8');
    const lines = content.split('\n').filter(line => line.trim() && !line.startsWith('#'));
    fileRequests = lines.map(line => {
      try {
        return JSON.parse(line);
      } catch (e) {
        return null;
      }
    }).filter(item => item !== null);
  } catch (error) {
    // In serverless environments, file system may not be available
    if (error.code === 'ENOENT' || error.code === 'EROFS' || error.code === 'EPERM') {
      console.log('File read skipped (serverless):', error.code);
    } else {
      console.log('File read error (non-critical):', error.message);
    }
  }
  
  // Combine file requests with in-memory requests
  // Use a Map to deduplicate by requestId
  const allRequestsMap = new Map();
  
  // Add file requests first
  for (const req of fileRequests) {
    if (req.requestId) {
      allRequestsMap.set(req.requestId, req);
    }
  }
  
  // Add in-memory requests (will overwrite if duplicate)
  for (const req of inMemoryDemoRequests) {
    if (req.requestId) {
      allRequestsMap.set(req.requestId, req);
    }
  }
  
  const allRequests = Array.from(allRequestsMap.values());
  console.log(`Total requests: ${allRequests.length} (${fileRequests.length} from file, ${inMemoryDemoRequests.length} in memory)`);
  
  return allRequests;
};

const writeDemoRequest = async (data) => {
  // Always store in memory first (works in serverless environment)
  inMemoryDemoRequests.push(data);
  console.log('Demo request stored in memory. Total in-memory requests:', inMemoryDemoRequests.length);
  
  // Try to also write to file (will fail in serverless, but works locally)
  const line = JSON.stringify(data) + '\n';
  try {
    await fs.appendFile(DEMO_REQUESTS_FILE, line, 'utf-8');
    console.log('Demo request also written to file');
  } catch (error) {
    // If file doesn't exist, try to create it
    if (error.code === 'ENOENT') {
      try {
        const header = '# Demo Requests Storage\n# Format: One JSON object per line\n# Each line contains: requestId, email, userType, socialHandle, socialPlatform, source, timestamp, ipAddress, createdAt\n\n';
        await fs.writeFile(DEMO_REQUESTS_FILE, header + line, 'utf-8');
      } catch (writeError) {
        // Ignore - serverless environment, data is in memory
        console.log('File write skipped (serverless):', writeError.code);
      }
    } else {
      // Read-only filesystem (EROFS) or permission denied (EPERM) - expected in Vercel
      console.log('File write skipped (serverless):', error.code);
    }
  }
};

const checkDuplicate = async (email) => {
  const requests = await readDemoRequests();
  const normalizedEmail = normalizeEmail(email);
  const thirtyDaysAgo = Date.now() - (30 * 24 * 60 * 60 * 1000);
  
  const duplicate = requests.find(req => {
    if (normalizeEmail(req.email) !== normalizedEmail) return false;
    const createdAt = new Date(req.createdAt).getTime();
    return createdAt > thirtyDaysAgo;
  });
  
  return duplicate || null;
};

const checkRateLimit = (ip, email) => {
  const now = Date.now();
  const oneHour = 60 * 60 * 1000;
  const oneDay = 24 * 60 * 60 * 1000;
  
  // Check IP limits
  if (!rateLimitStore.ip[ip]) {
    rateLimitStore.ip[ip] = { requests: [] };
  }
  const ipRequests = rateLimitStore.ip[ip].requests.filter(time => now - time < oneDay);
  const ipRequestsLastHour = ipRequests.filter(time => now - time < oneHour);
  
  if (ipRequestsLastHour.length >= 10) {
    return { limited: true, type: 'ip', limit: 'hour', retryAfter: 3600 };
  }
  if (ipRequests.length >= 50) {
    return { limited: true, type: 'ip', limit: 'day', retryAfter: 86400 };
  }
  
  // Check email limits
  const normalizedEmail = normalizeEmail(email);
  if (!rateLimitStore.email[normalizedEmail]) {
    rateLimitStore.email[normalizedEmail] = { requests: [] };
  }
  const emailRequests = rateLimitStore.email[normalizedEmail].requests.filter(
    time => now - time < oneDay
  );
  
  if (emailRequests.length >= 3) {
    return { limited: true, type: 'email', limit: 'day', retryAfter: 86400 };
  }
  
  // Update rate limit stores
  rateLimitStore.ip[ip].requests.push(now);
  rateLimitStore.email[normalizedEmail].requests.push(now);
  
  return { limited: false };
};

// Root endpoint
app.get('/', (req, res) => {
  res.json({
    name: 'AW3 Platform Mock API',
    version: '1.0.0',
    documentation: '/docs',
    endpoints: {
      swagger: {
        yaml: '/swagger.yaml',
        json: '/swagger.json',
        ui: '/docs'
      },
      health: '/health'
    }
  });
});

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ============ CREATOR PROFILE ENDPOINTS ============
// NOTE: Authentication is handled entirely by Privy on the frontend.
// Backend validates the Privy ACCESS_TOKEN from the Authorization header.
// No /auth/* endpoints are needed.
app.get('/api/creator/profile/me', (req, res) => {
  res.json(apiResponse({
    userId: uuidv4(),
    walletAddress: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e',
    displayName: 'CryptoCreator',
    avatar: 'https://i.pravatar.cc/150?u=creator1',
    bio: 'Web3 content creator specializing in DeFi and NFT projects',
    focusArea: [1, 2, 6],
    socialAccounts: [
      {
        platform: 1,
        handle: '@cryptoinfluencer',
        link: 'https://twitter.com/cryptoinfluencer',
        followers: 125000,
        verified: true,
        verifiedAt: '2024-01-15T10:00:00Z'
      },
      {
        platform: 2,
        handle: '@cryptovideos',
        link: 'https://youtube.com/@cryptovideos',
        followers: 85000,
        verified: true,
        verifiedAt: '2024-01-20T10:00:00Z'
      }
    ],
    profileComplete: true,
    createdAt: '2024-01-10T10:00:00Z'
  }));
});

app.put('/api/creator/profile/me', (req, res) => {
  const { displayName, avatar, bio, focusArea } = req.body;
  res.json(apiResponse({
    userId: uuidv4(),
    walletAddress: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e',
    displayName: displayName || 'CryptoCreator',
    avatar: avatar || 'https://i.pravatar.cc/150?u=creator1',
    bio: bio || 'Web3 content creator',
    focusArea: focusArea || [1, 2],
    socialAccounts: [],
    profileComplete: true,
    createdAt: '2024-01-10T10:00:00Z'
  }));
});

app.post('/api/creator/profile/social-verification', (req, res) => {
  const { platform, handle } = req.body;
  res.json(apiResponse({
    platform,
    handle,
    verificationStatus: 'PENDING',
    message: 'Verification initiated. Please check your social media for instructions.'
  }));
});

// ============ CREATOR CAMPAIGNS ENDPOINTS ============
app.get('/api/creator/campaigns', (req, res) => {
  const mockCreatorProfile = {
    focusArea: [1, 2],
    reputation: 87.5
  };

  const campaigns = [
    {
      campaignId: uuidv4(),
      projectId: uuidv4(),
      title: 'DeFi Protocol Launch Campaign',
      focusArea: 1,
      status: 3,
      budgetAmount: 5000,
      budgetToken: 1,
      numberOfApplicants: 24,
      numberOfDeliveries: 5,
      deadline: '2025-01-15T23:59:59Z',
      kpiTargets: { views: 50000, engagement: 5 },
      requiredReputation: 75,
      matchRate: calculateMatchRate({ ...mockCreatorProfile, reputation: 87.5 }, { 
        focusArea: 1, 
        requiredReputation: 75, 
        kpiTargets: {} 
      }),
      projectAvatar: 'https://i.pravatar.cc/150?u=project1',
      createdAt: '2024-12-01T10:00:00Z',
      updatedAt: '2024-12-10T10:00:00Z'
    },
    {
      campaignId: uuidv4(),
      projectId: uuidv4(),
      title: 'NFT Collection Promotion',
      focusArea: 2,
      status: 3,
      budgetAmount: 3500,
      budgetToken: 1,
      numberOfApplicants: 18,
      numberOfDeliveries: 8,
      deadline: '2025-01-20T23:59:59Z',
      kpiTargets: { views: 30000, engagement: 4 },
      requiredReputation: 70,
      matchRate: calculateMatchRate({ ...mockCreatorProfile, reputation: 87.5 }, { 
        focusArea: 2, 
        requiredReputation: 70, 
        kpiTargets: {} 
      }),
      projectAvatar: 'https://i.pravatar.cc/150?u=project2',
      createdAt: '2024-12-02T10:00:00Z',
      updatedAt: '2024-12-09T10:00:00Z'
    },
    {
      campaignId: uuidv4(),
      projectId: uuidv4(),
      title: 'Gaming Platform Beta Test',
      focusArea: 3,
      status: 3,
      budgetAmount: 8000,
      budgetToken: 1,
      numberOfApplicants: 42,
      numberOfDeliveries: 10,
      deadline: '2025-02-01T23:59:59Z',
      kpiTargets: { signups: 1000, engagement: 6 },
      requiredReputation: 80,
      matchRate: calculateMatchRate({ ...mockCreatorProfile, reputation: 87.5 }, { 
        focusArea: 3, 
        requiredReputation: 80, 
        kpiTargets: {} 
      }),
      projectAvatar: 'https://i.pravatar.cc/150?u=project3',
      createdAt: '2024-12-03T10:00:00Z',
      updatedAt: '2024-12-08T10:00:00Z'
    }
  ];

  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;

  res.json(apiResponse({
    campaigns,
    pagination: {
      currentPage: page,
      totalPages: 3,
      totalElements: 48,
      pageSize: size
    }
  }));
});

app.get('/api/creator/campaigns/:id', (req, res) => {
  res.json(apiResponse({
    campaignId: req.params.id,
    projectId: uuidv4(),
    title: 'DeFi Protocol Launch Campaign',
    description: 'Help us launch our revolutionary DeFi protocol with engaging content that educates and attracts users.',
    objective: 'Increase platform awareness and drive user signups through authentic creator content showcasing our unique features and benefits.',
    focusArea: 1,
    status: 3,
    budgetAmount: 5000,
    budgetToken: 1,
    numberOfCreators: 10,
    numberOfApplicants: 24,
    numberOfDeliveries: 5,
    deadline: '2025-01-15T23:59:59Z',
    complexity: 2,
    kpiTargets: {
      views: 50000,
      engagement: 5,
      conversions: 500
    },
    requiredReputation: 75,
    matchRate: 92.5,
    projectInfo: {
      projectId: uuidv4(),
      projectName: 'DefiMax Protocol',
      projectAvatar: 'https://i.pravatar.cc/150?u=project1',
      website: 'https://defimax.io',
      socialChannels: [
        {
          platform: 1,
          handle: '@DefiMax',
          link: 'https://twitter.com/defimax',
          followers: 45000,
          verified: true,
          verifiedAt: '2024-01-01T00:00:00Z'
        },
        {
          platform: 5,
          handle: 'DefiMax Community',
          link: 'https://discord.gg/defimax',
          followers: 12000,
          verified: true,
          verifiedAt: '2024-01-01T00:00:00Z'
        }
      ]
    },
    paymentTerms: {
      paymentMethod: 'USDC on Ethereum mainnet via smart contract escrow',
      paymentSchedule: 'Milestone-based: 50% upon content approval, 50% after 7 days performance verification',
      paymentConditions: 'Content must meet quality standards and achieve minimum 70% of target KPIs'
    },
    createdAt: '2024-12-01T10:00:00Z',
    updatedAt: '2024-12-10T10:00:00Z'
  }));
});

// ============ CREATOR APPLICATIONS ENDPOINTS ============
app.get('/api/creator/applications', (req, res) => {
  res.json(apiResponse([
    {
      applicationId: uuidv4(),
      campaignId: uuidv4(),
      campaignTitle: 'DeFi Protocol Launch Campaign',
      creatorId: uuidv4(),
      proposedRate: 500,
      proposal: 'I have extensive experience promoting DeFi projects with proven track record...',
      status: 1,
      portfolioLinks: ['https://youtube.com/video1', 'https://twitter.com/post1'],
      relevantExperience: '3 years in Web3 content creation',
      estimatedCompletionDays: 7,
      matchScore: 92.5,
      appliedAt: '2024-12-08T10:00:00Z',
      reviewedAt: null
    },
    {
      applicationId: uuidv4(),
      campaignId: uuidv4(),
      campaignTitle: 'NFT Collection Promotion',
      creatorId: uuidv4(),
      proposedRate: 400,
      proposal: 'My audience loves NFT content...',
      status: 2,
      portfolioLinks: ['https://youtube.com/video2'],
      relevantExperience: '2 years NFT content',
      estimatedCompletionDays: 5,
      matchScore: 88.0,
      appliedAt: '2024-12-05T10:00:00Z',
      reviewedAt: '2024-12-07T10:00:00Z'
    }
  ]));
});

app.post('/api/creator/applications', (req, res) => {
  const { campaignId, proposedRate, proposal, portfolioLinks, relevantExperience, estimatedCompletionDays } = req.body;
  res.json(apiResponse({
    applicationId: uuidv4(),
    campaignId,
    creatorId: uuidv4(),
    proposedRate,
    proposal,
    status: 1,
    portfolioLinks,
    relevantExperience,
    estimatedCompletionDays,
    matchScore: 85.0,
    appliedAt: new Date().toISOString(),
    reviewedAt: null
  }));
});

app.get('/api/creator/applications/:id', (req, res) => {
  res.json(apiResponse({
    applicationId: req.params.id,
    campaignId: uuidv4(),
    campaignTitle: 'DeFi Protocol Launch Campaign',
    creatorId: uuidv4(),
    proposedRate: 500,
    proposal: 'I have extensive experience promoting DeFi projects...',
    status: 1,
    portfolioLinks: ['https://youtube.com/video1'],
    relevantExperience: '3 years in Web3',
    estimatedCompletionDays: 7,
    matchScore: 92.5,
    appliedAt: '2024-12-08T10:00:00Z',
    reviewedAt: null
  }));
});

// ============ CREATOR DELIVERABLES ENDPOINTS ============
app.get('/api/creator/deliverables', (req, res) => {
  res.json(apiResponse([
    {
      deliverableId: uuidv4(),
      campaignId: uuidv4(),
      creatorId: uuidv4(),
      contentUrl: 'https://youtube.com/watch?v=abc123',
      deliverableType: 2,
      platform: 2,
      status: 3,
      metrics: {
        views: 52000,
        likes: 3200,
        comments: 450,
        shares: 280,
        engagementRate: 7.5
      },
      cvpiScore: 88.5,
      paymentAmount: 500,
      submittedAt: '2024-12-05T10:00:00Z',
      verifiedAt: '2024-12-07T10:00:00Z'
    }
  ]));
});

app.post('/api/creator/deliverables', (req, res) => {
  const { campaignId, contentUrl, deliverableType, platform, description } = req.body;
  res.json(apiResponse({
    deliverableId: uuidv4(),
    campaignId,
    creatorId: uuidv4(),
    contentUrl,
    deliverableType,
    platform,
    status: 1,
    submittedAt: new Date().toISOString(),
    verifiedAt: null
  }));
});

app.get('/api/creator/deliverables/:id', (req, res) => {
  res.json(apiResponse({
    deliverableId: req.params.id,
    campaignId: uuidv4(),
    creatorId: uuidv4(),
    contentUrl: 'https://youtube.com/watch?v=abc123',
    deliverableType: 2,
    platform: 2,
    status: 3,
    metrics: {
      views: 52000,
      likes: 3200,
      comments: 450,
      shares: 280,
      engagementRate: 7.5
    },
    cvpiScore: 88.5,
    paymentAmount: 500,
    submittedAt: '2024-12-05T10:00:00Z',
    verifiedAt: '2024-12-07T10:00:00Z'
  }));
});

// ============ CREATOR EARNINGS ENDPOINTS ============
app.get('/api/creator/earnings', (req, res) => {
  res.json(apiResponse({
    totalEarned: 12450.50,
    pendingPayments: 1500.00,
    availableBalance: 10950.50,
    currency: 'USDC',
    averageROI: 45.8,
    growthRate: 12.5
  }));
});

app.get('/api/creator/earnings/history', (req, res) => {
  const range = parseInt(req.query.range) || 3;
  
  const generateData = (rangeType) => {
    const data = [];
    const now = new Date();
    let points = 30;
    
    if (rangeType === 1) points = 24; // Hourly
    if (rangeType === 2) points = 30; // Daily
    if (rangeType === 3) points = 12; // Monthly
    
    for (let i = points; i >= 0; i--) {
      const timestamp = new Date(now);
      if (rangeType === 1) timestamp.setHours(now.getHours() - i);
      if (rangeType === 2) timestamp.setDate(now.getDate() - i);
      if (rangeType === 3) timestamp.setMonth(now.getMonth() - i);
      
      data.push({
        timestamp: timestamp.toISOString(),
        amount: Math.random() * 500 + 100,
        transactionCount: Math.floor(Math.random() * 5) + 1
      });
    }
    return data;
  };

  res.json(apiResponse({
    data: generateData(range),
    range,
    totalAmount: 12450.50,
    periodStart: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
    periodEnd: new Date().toISOString()
  }));
});

// ============ CREATOR CVPI ENDPOINTS ============
app.get('/api/creator/cvpi/score', (req, res) => {
  res.json(apiResponse({
    userId: uuidv4(),
    overallScore: 82.3,
    components: {
      engagement: 85.0,
      reach: 78.5,
      conversion: 88.2,
      consistency: 80.0,
      quality: 79.8
    },
    reputation: {
      score: 87.5,
      tier: 'GOLD',
      totalReviews: 42,
      averageRating: 4.7
    },
    trend: 'UP',
    percentile: 87,
    lastUpdated: new Date().toISOString()
  }));
});

app.get('/api/creator/cvpi/history', (req, res) => {
  const period = parseInt(req.query.period) || 2;
  const limit = parseInt(req.query.limit) || 30;
  
  const generateHistory = (periodType, limitCount) => {
    const history = [];
    const now = new Date();
    
    let days = 7;
    if (periodType === 2) days = 30;
    if (periodType === 3) days = 90;
    if (periodType === 4) days = 365;
    
    const interval = Math.floor(days / Math.min(limitCount, days));
    
    for (let i = days; i >= 0; i -= interval) {
      const date = new Date(now);
      date.setDate(now.getDate() - i);
      history.push({
        date: date.toISOString().split('T')[0],
        score: 70 + Math.random() * 20,
        reputation: 75 + Math.random() * 15
      });
    }
    return history.slice(0, limitCount);
  };

  res.json(apiResponse({
    history: generateHistory(period, limit),
    period,
    limit
  }));
});

// ============ CREATOR CERTIFICATES ENDPOINTS ============
app.get('/api/creator/certificates', (req, res) => {
  res.json(apiResponse({
    certificates: [
      {
        certificateId: uuidv4(),
        certificateType: 'Top Performer',
        title: 'DeFi Campaign Excellence',
        issueDate: '2024-11-15',
        imageUrl: 'https://via.placeholder.com/800x600/4F46E5/FFFFFF?text=Top+Performer+Certificate',
        relatedCampaign: {
          campaignId: uuidv4(),
          campaignTitle: 'DeFi Protocol Launch Campaign',
          projectName: 'DefiMax Protocol'
        },
        metadata: {
          achievement: 'Exceeded KPIs by 150%',
          rank: '1/24'
        }
      },
      {
        certificateId: uuidv4(),
        certificateType: 'Quality Content',
        title: 'NFT Content Creation Award',
        issueDate: '2024-10-20',
        imageUrl: 'https://via.placeholder.com/800x600/7C3AED/FFFFFF?text=Quality+Content+Award',
        relatedCampaign: {
          campaignId: uuidv4(),
          campaignTitle: 'NFT Collection Promotion',
          projectName: 'ArtBlock NFTs'
        },
        metadata: {
          achievement: 'Outstanding content quality',
          rating: '4.9/5.0'
        }
      },
      {
        certificateId: uuidv4(),
        certificateType: 'High Engagement',
        title: 'Community Builder Recognition',
        issueDate: '2024-09-10',
        imageUrl: 'https://via.placeholder.com/800x600/059669/FFFFFF?text=High+Engagement+Award',
        relatedCampaign: {
          campaignId: uuidv4(),
          campaignTitle: 'Gaming Platform Beta Test',
          projectName: 'MetaGame Arena'
        },
        metadata: {
          achievement: '12% engagement rate',
          followers: '+5000'
        }
      }
    ]
  }));
});

// ============ CREATOR SETTINGS ENDPOINTS ============
app.get('/api/creator/settings/language', (req, res) => {
  res.json(apiResponse({
    language: 'en',
    timezone: 'UTC'
  }));
});

app.post('/api/creator/settings/language', (req, res) => {
  res.json(apiResponse({
    language: req.body.language || 'en',
    timezone: req.body.timezone || 'UTC'
  }));
});

app.get('/api/creator/settings/rate', (req, res) => {
  res.json(apiResponse({
    hourlyRate: 100,
    dailyRate: 800,
    projectRate: 5000,
    currency: 'USDC',
    minimumBudget: 500
  }));
});

app.post('/api/creator/settings/rate', (req, res) => {
  res.json(apiResponse({
    hourlyRate: req.body.hourlyRate || 100,
    dailyRate: req.body.dailyRate || 800,
    projectRate: req.body.projectRate || 5000,
    currency: req.body.currency || 'USDC',
    minimumBudget: req.body.minimumBudget || 500
  }));
});

app.get('/api/creator/settings/notification', (req, res) => {
  res.json(apiResponse({
    emailNotifications: true,
    pushNotifications: true,
    campaignUpdates: true,
    applicationUpdates: true,
    paymentNotifications: true,
    marketingEmails: false
  }));
});

app.post('/api/creator/settings/notification', (req, res) => {
  res.json(apiResponse({
    emailNotifications: req.body.emailNotifications !== undefined ? req.body.emailNotifications : true,
    pushNotifications: req.body.pushNotifications !== undefined ? req.body.pushNotifications : true,
    campaignUpdates: req.body.campaignUpdates !== undefined ? req.body.campaignUpdates : true,
    applicationUpdates: req.body.applicationUpdates !== undefined ? req.body.applicationUpdates : true,
    paymentNotifications: req.body.paymentNotifications !== undefined ? req.body.paymentNotifications : true,
    marketingEmails: req.body.marketingEmails !== undefined ? req.body.marketingEmails : false
  }));
});

app.get('/api/creator/settings/privacy', (req, res) => {
  res.json(apiResponse({
    profileVisibility: 'PUBLIC',
    showEarnings: false,
    showCompletedCampaigns: true,
    showSocialAccounts: true
  }));
});

app.post('/api/creator/settings/privacy', (req, res) => {
  res.json(apiResponse({
    profileVisibility: req.body.profileVisibility || 'PUBLIC',
    showEarnings: req.body.showEarnings !== undefined ? req.body.showEarnings : false,
    showCompletedCampaigns: req.body.showCompletedCampaigns !== undefined ? req.body.showCompletedCampaigns : true,
    showSocialAccounts: req.body.showSocialAccounts !== undefined ? req.body.showSocialAccounts : true
  }));
});

app.get('/api/creator/settings/security', (req, res) => {
  res.json(apiResponse({
    twoFactorEnabled: false,
    loginAlerts: true,
    trustedDevices: [
      {
        deviceId: uuidv4(),
        deviceName: 'Chrome on Windows',
        lastUsed: '2024-12-10T09:30:00Z'
      }
    ]
  }));
});

app.post('/api/creator/settings/security', (req, res) => {
  res.json(apiResponse({
    twoFactorEnabled: req.body.twoFactorEnabled !== undefined ? req.body.twoFactorEnabled : false,
    loginAlerts: req.body.loginAlerts !== undefined ? req.body.loginAlerts : true,
    trustedDevices: req.body.trustedDevices || []
  }));
});

// ============ DASHBOARD ENDPOINTS ============
app.get('/api/dashboard/trending', (req, res) => {
  res.json(apiResponse({
    campaigns: [
      {
        campaignId: uuidv4(),
        projectId: uuidv4(),
        title: 'Viral DeFi Launch',
        focusArea: 1,
        status: 3,
        budgetAmount: 10000,
        budgetToken: 1,
        numberOfApplicants: 67,
        numberOfDeliveries: 15,
        deadline: '2025-01-25T23:59:59Z',
        kpiTargets: {},
        requiredReputation: 85,
        matchRate: 95.0,
        projectAvatar: 'https://i.pravatar.cc/150?u=trending1',
        createdAt: '2024-12-08T10:00:00Z',
        updatedAt: '2024-12-10T10:00:00Z'
      },
      {
        campaignId: uuidv4(),
        projectId: uuidv4(),
        title: 'Trending NFT Drop',
        focusArea: 2,
        status: 3,
        budgetAmount: 7500,
        budgetToken: 1,
        numberOfApplicants: 52,
        numberOfDeliveries: 12,
        deadline: '2025-01-18T23:59:59Z',
        kpiTargets: {},
        requiredReputation: 80,
        matchRate: 88.5,
        projectAvatar: 'https://i.pravatar.cc/150?u=trending2',
        createdAt: '2024-12-09T10:00:00Z',
        updatedAt: '2024-12-10T10:00:00Z'
      }
    ],
    period: '24h'
  }));
});

app.get('/api/dashboard/live', (req, res) => {
  res.json(apiResponse({
    campaigns: [
      {
        campaignId: uuidv4(),
        projectId: uuidv4(),
        title: 'Live Gaming Tournament',
        focusArea: 3,
        status: 4,
        budgetAmount: 6000,
        budgetToken: 1,
        numberOfApplicants: 35,
        numberOfDeliveries: 20,
        deadline: '2025-01-30T23:59:59Z',
        kpiTargets: {},
        requiredReputation: 75,
        matchRate: 82.0,
        projectAvatar: 'https://i.pravatar.cc/150?u=live1',
        createdAt: '2024-12-05T10:00:00Z',
        updatedAt: '2024-12-10T10:00:00Z'
      }
    ],
    activeCount: 28
  }));
});

app.get('/api/dashboard/action-items', (req, res) => {
  res.json(apiResponse({
    items: [
      {
        id: uuidv4(),
        type: 'DELIVERABLE_SUBMIT',
        title: 'Submit deliverable for DeFi Campaign',
        description: 'Content deadline approaching in 2 days',
        priority: 'HIGH',
        deadline: '2024-12-12T23:59:59Z',
        relatedEntityId: uuidv4()
      },
      {
        id: uuidv4(),
        type: 'APPLICATION_REVIEW',
        title: 'Check application status',
        description: 'Your application has been reviewed',
        priority: 'MEDIUM',
        deadline: null,
        relatedEntityId: uuidv4()
      },
      {
        id: uuidv4(),
        type: 'PAYMENT_PENDING',
        title: 'Payment verification in progress',
        description: 'Your deliverable is being verified for payment',
        priority: 'LOW',
        deadline: null,
        relatedEntityId: uuidv4()
      }
    ]
  }));
});

app.get('/api/dashboard/analytics', (req, res) => {
  res.json(apiResponse({
    totalCampaigns: 1247,
    activeCampaigns: 186,
    totalCreators: 8954,
    totalValueProcessed: 12450000
  }));
});

// ============ FILTER ENDPOINTS ============
app.get('/api/filters/campaign-options', (req, res) => {
  res.json(apiResponse({
    focusAreas: [
      { id: 1, name: 'DeFi', count: 45 },
      { id: 2, name: 'NFT', count: 38 },
      { id: 3, name: 'Gaming', count: 32 },
      { id: 4, name: 'Infrastructure', count: 28 },
      { id: 5, name: 'L2', count: 22 },
      { id: 6, name: 'DAO', count: 18 },
      { id: 7, name: 'Metaverse', count: 15 },
      { id: 8, name: 'Trading', count: 12 },
      { id: 9, name: 'Other', count: 8 }
    ],
    deliverableTypes: [
      { id: 1, name: 'Twitter Posts', count: 62 },
      { id: 2, name: 'Videos', count: 48 },
      { id: 3, name: 'Articles', count: 35 },
      { id: 4, name: 'AMAs', count: 25 },
      { id: 5, name: 'Discord Management', count: 18 },
      { id: 6, name: 'Community Growth', count: 15 },
      { id: 7, name: 'Instagram Post', count: 12 },
      { id: 8, name: 'TikTok', count: 10 }
    ],
    durations: [
      { id: 1, name: 'Less Than 1 Week', count: 28 },
      { id: 2, name: '1-2 Weeks', count: 42 },
      { id: 3, name: '2-4 Weeks', count: 58 },
      { id: 4, name: '1-3 Months', count: 35 },
      { id: 5, name: '3+ Months', count: 18 }
    ],
    stages: [
      { id: 1, name: 'Language' },
      { id: 2, name: 'Rate Configuration' },
      { id: 3, name: 'Notification Preferences' },
      { id: 4, name: 'Privacy Controls' },
      { id: 5, name: 'Wallet & Payout' },
      { id: 6, name: 'Security' }
    ],
    budgetRanges: [
      { min: 0, max: 1000, label: '$0 - $1,000', count: 45 },
      { min: 1000, max: 5000, label: '$1,000 - $5,000', count: 78 },
      { min: 5000, max: 10000, label: '$5,000 - $10,000', count: 52 },
      { min: 10000, max: 50000, label: '$10,000 - $50,000', count: 28 },
      { min: 50000, max: null, label: '$50,000+', count: 15 }
    ]
  }));
});

// ============ PUBLIC MARKETPLACE ENDPOINTS ============
app.get('/api/public/marketplace/campaigns', (req, res) => {
  res.json(apiResponse({
    campaigns: [
      {
        campaignId: uuidv4(),
        title: 'Public DeFi Campaign',
        focusArea: 1,
        budgetAmount: 5000,
        budgetToken: 1,
        deadline: '2025-01-15T23:59:59Z'
      }
    ]
  }));
});

app.get('/api/public/marketplace/stats', (req, res) => {
  res.json(apiResponse({
    totalCampaigns: 1247,
    activeCampaigns: 186,
    totalCreators: 8954,
    totalProjects: 542,
    totalValueProcessed: 12450000,
    averageCampaignBudget: 4520
  }));
});

// ============ PROJECT DASHBOARD ENDPOINTS ============
app.get('/api/project/dashboard/stats', (req, res) => {
  res.json(apiResponse({
    activeCampaigns: 3,
    pendingApplications: 12,
    budgetAvailable: 25000,
    deliverablesSubmitted: 3,
    totalCampaigns: 28,
    totalSpent: 245000,
    reputationScore: 780,
    reputationTier: 'B',
    avgCampaignCVPI: 95.3
  }));
});

// ============ PROJECT CAMPAIGNS ENDPOINTS ============
app.get('/api/project/campaigns', (req, res) => {
  const campaigns = [
    {
      campaignId: uuidv4(),
      name: 'DeFi Protocol Launch',
      focusArea: 1,
      status: 3,
      budgetTotal: 10000,
      budgetRemaining: 8000,
      applicationCount: 45,
      approvedCount: 3,
      deliverableCount: 2,
      daysRemaining: 12,
      cvpiScore: 85.3,
      cvpiClassification: 'Good',
      progressStage: 'InProgress',
      createdAt: '2024-12-01T10:00:00Z',
      endDate: '2025-01-15T23:59:59Z'
    },
    {
      campaignId: uuidv4(),
      name: 'NFT Collection Drop',
      focusArea: 2,
      status: 3,
      budgetTotal: 7000,
      budgetRemaining: 5500,
      applicationCount: 32,
      approvedCount: 2,
      deliverableCount: 1,
      daysRemaining: 18,
      cvpiScore: 78.6,
      cvpiClassification: 'Good',
      progressStage: 'Verification',
      createdAt: '2024-11-20T10:00:00Z',
      endDate: '2025-01-20T23:59:59Z'
    },
    {
      campaignId: uuidv4(),
      name: 'Gaming Partnership',
      focusArea: 3,
      status: 5,
      budgetTotal: 8500,
      budgetRemaining: 0,
      applicationCount: 28,
      approvedCount: 2,
      deliverableCount: 2,
      daysRemaining: null,
      cvpiScore: 92.1,
      cvpiClassification: 'Good',
      progressStage: 'Completed',
      createdAt: '2024-10-15T10:00:00Z',
      endDate: '2024-11-30T23:59:59Z'
    }
  ];

  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;

  res.json(apiResponse({
    campaigns,
    pagination: {
      currentPage: page,
      totalPages: 2,
      totalElements: 28,
      pageSize: size
    }
  }));
});

app.post('/api/project/campaigns', (req, res) => {
  const campaignId = uuidv4();
  res.json(apiResponse({
    campaignId,
    transactionHash: '0x8a2f' + Math.random().toString(36).substring(2, 15),
    status: 'PENDING',
    estimatedConfirmationTime: 30,
    totalLocked: req.body.creatorBudget * 1.12
  }));
});

app.get('/api/project/campaigns/:id', (req, res) => {
  res.json(apiResponse({
    campaignId: req.params.id,
    projectId: uuidv4(),
    name: 'DeFi Protocol Launch',
    description: 'Help us launch our revolutionary DeFi protocol with engaging content that educates and attracts users.',
    objective: 'Increase platform awareness and drive user signups through authentic creator content showcasing our unique features and benefits.',
    focusArea: 1,
    status: 3,
    budgetAmount: 10000,
    budgetToken: 1,
    numberOfCreators: 3,
    numberOfApplicants: 45,
    numberOfDeliveries: 5,
    deadline: '2025-01-15T23:59:59Z',
    complexity: 2,
    kpiTargets: {
      views: 50000,
      engagement: 5,
      conversions: 500
    },
    requiredReputation: 75,
    matchRate: 92.5,
    projectInfo: {
      projectId: uuidv4(),
      projectName: 'DefiMax Protocol',
      projectAvatar: 'https://i.pravatar.cc/150?u=project1',
      website: 'https://defimax.io',
      socialChannels: [
        {
          platform: 1,
          handle: '@DefiMax',
          link: 'https://twitter.com/defimax',
          followers: 45000,
          verified: true,
          verifiedAt: '2024-01-01T00:00:00Z'
        }
      ]
    },
    paymentTerms: {
      paymentMethod: 'USDC on Ethereum mainnet via smart contract escrow',
      paymentSchedule: 'Milestone-based: 50% upon content approval, 50% after 7 days performance verification',
      paymentConditions: 'Content must meet quality standards and achieve minimum 70% of target KPIs'
    },
    createdAt: '2024-12-01T10:00:00Z',
    updatedAt: '2024-12-10T10:00:00Z'
  }));
});

app.put('/api/project/campaigns/:id', (req, res) => {
  res.json(apiResponse({
    message: 'Campaign updated successfully'
  }));
});

app.delete('/api/project/campaigns/:id', (req, res) => {
  res.json(apiResponse({
    message: 'Campaign deleted successfully'
  }));
});

app.post('/api/project/campaigns/:id/pause', (req, res) => {
  res.json(apiResponse({
    message: 'Campaign paused',
    status: 'PAUSED'
  }));
});

app.post('/api/project/campaigns/:id/resume', (req, res) => {
  res.json(apiResponse({
    message: 'Campaign resumed',
    status: 'ACTIVE'
  }));
});

app.post('/api/project/campaigns/:id/extend', (req, res) => {
  res.json(apiResponse({
    message: 'Campaign extended',
    newEndDate: req.body.newEndDate
  }));
});

app.post('/api/project/campaigns/:id/invite', (req, res) => {
  res.json(apiResponse({
    message: `${req.body.creatorIds.length} invitations sent`,
    invited: req.body.creatorIds
  }));
});

app.get('/api/project/campaigns/:id/export', (req, res) => {
  res.json(apiResponse({
    message: 'Report generation initiated',
    format: req.query.format,
    downloadUrl: `/downloads/campaign-${req.params.id}.${req.query.format.toLowerCase()}`
  }));
});

app.get('/api/project/campaigns/:id/metrics', (req, res) => {
  res.json(apiResponse({
    budgetStatus: {
      total: 10000,
      remaining: 8000,
      spent: 2000,
      percentageUsed: 20
    },
    applications: {
      received: 45,
      approved: 3,
      pendingReview: 12,
      rejected: 30
    },
    progress: {
      status: 3,
      daysElapsed: 18,
      daysTotal: 30,
      percentageComplete: 60,
      endDate: '2025-01-15T23:59:59Z'
    },
    cvpiScore: {
      current: 85.3,
      classification: 'Good',
      trend: 3.2
    }
  }));
});

app.get('/api/project/campaigns/:id/overview', (req, res) => {
  res.json(apiResponse({
    campaignId: req.params.id,
    details: {
      name: 'DeFi Protocol Launch',
      description: 'Help us launch our revolutionary DeFi protocol',
      deliverableRequirements: ['Twitter Thread', 'YouTube Video', 'Blog Article'],
      contentGuidelines: 'Focus on education, positive tone, include #DeFi hashtags'
    },
    kpiTargets: [
      {
        kpiName: 'Engagement Rate',
        target: 7.5,
        weight: 40,
        currentAverage: 8.2,
        status: 'Exceeding'
      },
      {
        kpiName: 'Reach',
        target: 200000,
        weight: 30,
        currentAverage: 245000,
        status: 'Exceeding'
      },
      {
        kpiName: 'Conversions',
        target: 350,
        weight: 30,
        currentAverage: 280,
        status: 'BelowTarget'
      }
    ],
    approvedCreators: [
      {
        creatorId: uuidv4(),
        name: 'CryptoCreator',
        avatar: 'https://i.pravatar.cc/150?u=creator1',
        reputation: 850,
        payment: 3333,
        deliverableStatus: 3,
        cvpiContribution: 78.5
      }
    ]
  }));
});

app.get('/api/project/campaigns/:id/analytics', (req, res) => {
  res.json(apiResponse({
    summary: {
      totalSpend: 10762,
      avgCVPI: 85.3,
      kpiSuccessRate: 87,
      avgCreatorReputation: 828
    },
    cvpiBreakdown: [
      {
        creatorName: 'Creator A',
        cvpiScore: 78.5,
        classification: 'Good'
      },
      {
        creatorName: 'Creator B',
        cvpiScore: 92.1,
        classification: 'Good'
      }
    ],
    kpiAchievement: [
      {
        date: '2024-12-01',
        kpiName: 'Engagement Rate',
        achievementPercentage: 109
      },
      {
        date: '2024-12-08',
        kpiName: 'Reach',
        achievementPercentage: 123
      }
    ],
    audienceReach: {
      totalImpressions: 735000,
      uniqueReach: 612000,
      totalEngagements: 58300,
      avgEngagementRate: 7.9
    },
    costEfficiency: {
      costPer1KImpressions: 14.65,
      costPerEngagement: 0.18,
      costPerConversion: 26.25
    }
  }));
});

app.get('/api/project/campaigns/:id/financials', (req, res) => {
  res.json(apiResponse({
    escrowOverview: {
      smartContractAddress: '0x742d35Cc6634C0532925a3b844Bc454e4438f44e',
      chain: 'BASE Mainnet',
      totalLocked: 11922,
      totalReleased: 3505,
      remainingBalance: 8417,
      expectedRefund: 1084
    },
    budgetAllocation: {
      creatorPayments: 10000,
      serviceFees: 768,
      oracleFees: 70,
      escrowBuffer: 1084,
      unused: 500
    },
    paymentHistory: [
      {
        date: '2024-11-23T10:30:00Z',
        type: 'CreatorPayment',
        recipient: 'Creator A',
        amount: 3333,
        status: 'Completed',
        transactionHash: '0x3f7a9e2b...'
      },
      {
        date: '2024-11-23T10:31:00Z',
        type: 'ServiceFee',
        recipient: 'AW3 Platform',
        amount: 256,
        status: 'Completed',
        transactionHash: '0x9c1d4f8a...'
      }
    ],
    feeBreakdown: {
      baseRate: 8,
      complexityMultiplier: 1.5,
      reputationDiscount: -20,
      tokenPaymentDiscount: -20,
      effectiveRate: 7.68,
      totalSaved: 432
    }
  }));
});

// ============ PROJECT APPLICATIONS ENDPOINTS ============
app.get('/api/project/applications', (req, res) => {
  const applications = [
    {
      applicationId: uuidv4(),
      campaignId: uuidv4(),
      campaignName: 'DeFi Protocol Launch',
      creatorId: uuidv4(),
      creatorName: 'CryptoInfluencer',
      creatorAvatar: 'https://i.pravatar.cc/150?u=creator1',
      reputation: 850,
      reputationTier: 'A',
      avgCVPI: 68.5,
      cvpiClassification: 'Excellent',
      socialStats: [
        { platform: 1, followers: 125000 },
        { platform: 5, followers: 2300 }
      ],
      campaignsCompleted: 42,
      successRate: 95,
      applicationMessage: 'I have extensive experience promoting DeFi projects with proven track record...',
      portfolioLinks: [
        {
          url: 'https://youtube.com/video1',
          title: 'DeFi Explained',
          description: 'Educational video about DeFi protocols'
        }
      ],
      appliedAt: '2024-12-08T10:00:00Z',
      status: 1,
      matchScore: 92.5
    },
    {
      applicationId: uuidv4(),
      campaignId: uuidv4(),
      campaignName: 'DeFi Protocol Launch',
      creatorId: uuidv4(),
      creatorName: 'BlockchainExpert',
      creatorAvatar: 'https://i.pravatar.cc/150?u=creator2',
      reputation: 780,
      reputationTier: 'B',
      avgCVPI: 75.2,
      cvpiClassification: 'Good',
      socialStats: [
        { platform: 1, followers: 85000 }
      ],
      campaignsCompleted: 28,
      successRate: 89,
      applicationMessage: 'My audience is highly engaged with DeFi content...',
      portfolioLinks: [],
      appliedAt: '2024-12-07T15:30:00Z',
      status: 1,
      matchScore: 85.0
    }
  ];

  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;

  res.json(apiResponse({
    applications,
    pagination: {
      currentPage: page,
      totalPages: 3,
      totalElements: 45,
      pageSize: size
    }
  }));
});

app.get('/api/project/applications/:id', (req, res) => {
  res.json(apiResponse({
    applicationId: req.params.id,
    campaignId: uuidv4(),
    campaignName: 'DeFi Protocol Launch',
    creatorId: uuidv4(),
    creatorName: 'CryptoInfluencer',
    creatorAvatar: 'https://i.pravatar.cc/150?u=creator1',
    reputation: 850,
    reputationTier: 'A',
    avgCVPI: 68.5,
    cvpiClassification: 'Excellent',
    socialStats: [
      { platform: 1, followers: 125000 },
      { platform: 5, followers: 2300 }
    ],
    campaignsCompleted: 42,
    successRate: 95,
    applicationMessage: 'I have extensive experience promoting DeFi projects with proven track record of delivering high engagement rates and conversions.',
    portfolioLinks: [
      {
        url: 'https://youtube.com/video1',
        title: 'DeFi Explained',
        description: 'Educational video about DeFi protocols'
      }
    ],
    appliedAt: '2024-12-08T10:00:00Z',
    status: 1,
    matchScore: 92.5
  }));
});

app.post('/api/project/applications/approve', (req, res) => {
  const successCount = req.body.applicationIds.length;
  res.json(apiResponse({
    successCount,
    failedIds: [],
    errors: []
  }));
});

app.post('/api/project/applications/reject', (req, res) => {
  const successCount = req.body.applicationIds.length;
  res.json(apiResponse({
    successCount,
    failedIds: [],
    errors: []
  }));
});

app.get('/api/project/applications/:id/deliverables', (req, res) => {
  res.json(apiResponse({
    deliverables: [
      {
        deliverableId: uuidv4(),
        contentUrl: 'https://twitter.com/user/status/123',
        deliverableType: 1,
        platform: 1,
        status: 3,
        submittedAt: '2024-12-10T10:00:00Z'
      }
    ]
  }));
});

// ============ PROJECT DELIVERABLES ENDPOINTS ============
app.get('/api/project/deliverables', (req, res) => {
  res.json(apiResponse({
    deliverables: [
      {
        deliverableId: uuidv4(),
        campaignId: uuidv4(),
        creatorId: uuidv4(),
        contentUrl: 'https://youtube.com/watch?v=abc123',
        deliverableType: 2,
        platform: 2,
        status: 2,
        metrics: {
          views: 52000,
          likes: 3200,
          comments: 450,
          shares: 280,
          engagementRate: 7.5
        },
        cvpiScore: 88.5,
        paymentAmount: 3333,
        submittedAt: '2024-12-05T10:00:00Z',
        verifiedAt: null
      }
    ]
  }));
});

app.get('/api/project/deliverables/:id', (req, res) => {
  res.json(apiResponse({
    deliverableId: req.params.id,
    campaignId: uuidv4(),
    creatorId: uuidv4(),
    creatorName: 'CryptoInfluencer',
    contentUrl: 'https://youtube.com/watch?v=abc123',
    deliverableType: 2,
    platform: 2,
    status: 2,
    metrics: {
      views: 52000,
      likes: 3200,
      comments: 450,
      shares: 280,
      engagementRate: 7.5
    },
    oracleVerification: {
      status: 'Complete',
      verifiedBy: 'Oracle Node #12',
      verifiedAt: '2024-11-23T10:30:00Z'
    },
    kpiResults: [
      {
        kpi: 'Engagement Rate',
        target: 7.5,
        actual: 8.2,
        achievement: 109,
        status: 'Met'
      },
      {
        kpi: 'Reach',
        target: 200000,
        actual: 245000,
        achievement: 123,
        status: 'Exceeded'
      }
    ],
    cvpiScore: 88.5,
    paymentAmount: 3333,
    submittedAt: '2024-12-05T10:00:00Z',
    verifiedAt: null
  }));
});

app.post('/api/project/deliverables/:id/verify', (req, res) => {
  res.json(apiResponse({
    message: 'Payment release initiated',
    transactionHash: '0x8a2f' + Math.random().toString(36).substring(2, 15),
    amount: 3333
  }));
});

app.post('/api/project/deliverables/:id/request-revision', (req, res) => {
  res.json(apiResponse({
    message: 'Revision requested',
    feedback: req.body.feedback
  }));
});

app.post('/api/project/deliverables/:id/reject', (req, res) => {
  res.json(apiResponse({
    message: 'Deliverable rejected',
    reason: req.body.reason,
    disputeInitiated: true
  }));
});

// ============ PROJECT CREATORS ENDPOINTS ============
app.get('/api/project/creators/discover', (req, res) => {
  const creators = [
    {
      creatorId: uuidv4(),
      displayName: 'CryptoInfluencer',
      avatar: 'https://i.pravatar.cc/150?u=creator1',
      reputation: 850,
      reputationTier: 'A',
      avgCVPI: 68.5,
      cvpiClassification: 'Excellent',
      socialStats: [
        { platform: 1, followers: 125000 },
        { platform: 2, followers: 85000 }
      ],
      campaignsCompleted: 42,
      successRate: 95,
      verticalExperience: [1, 2],
      estimatedCVPI: 72.3,
      available: true
    },
    {
      creatorId: uuidv4(),
      displayName: 'BlockchainGuru',
      avatar: 'https://i.pravatar.cc/150?u=creator2',
      reputation: 920,
      reputationTier: 'S',
      avgCVPI: 55.8,
      cvpiClassification: 'Excellent',
      socialStats: [
        { platform: 1, followers: 250000 },
        { platform: 2, followers: 150000 }
      ],
      campaignsCompleted: 78,
      successRate: 97,
      verticalExperience: [1, 4],
      estimatedCVPI: 58.2,
      available: true
    }
  ];

  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;

  res.json(apiResponse({
    creators,
    pagination: {
      currentPage: page,
      totalPages: 8,
      totalElements: 142,
      pageSize: size
    }
  }));
});

app.get('/api/project/creators/recommended', (req, res) => {
  res.json(apiResponse({
    creators: [
      {
        creatorId: uuidv4(),
        displayName: 'RecommendedCreator',
        avgCVPI: 62.5,
        reputation: 880,
        reason: 'Excellent performance in similar DeFi campaigns'
      }
    ]
  }));
});

app.get('/api/project/creators/:id', (req, res) => {
  res.json(apiResponse({
    creatorId: req.params.id,
    displayName: 'CryptoInfluencer',
    avatar: 'https://i.pravatar.cc/150?u=creator1',
    bio: 'Web3 content creator specializing in DeFi and NFT projects',
    reputation: 850,
    reputationTier: 'A',
    avgCVPI: 68.5,
    cvpiClassification: 'Excellent',
    socialStats: [
      { platform: 1, followers: 125000 },
      { platform: 2, followers: 85000 }
    ],
    campaignsCompleted: 42,
    successRate: 95,
    verticalExperience: [1, 2],
    available: true
  }));
});

// ============ PROJECT ANALYTICS ENDPOINTS ============
app.get('/api/project/analytics/overview', (req, res) => {
  res.json(apiResponse({
    totalSpend: 245000,
    avgCVPI: 95.3,
    campaignSuccessRate: 92,
    totalReach: 2800000,
    periodComparison: {
      previousPeriod: 18,
      platformAverage: 12,
      verticalAverage: 8
    }
  }));
});

// ============ ADMIN DASHBOARD ============
app.get('/api/admin/dashboard/stats', (req, res) => {
  res.json(apiResponse({
    totalCreators: 1240,
    totalProjects: 480,
    totalCampaigns: 2875,
    activeDisputes: 24,
    platformRevenue30d: 125000,
    treasuryBalance: 450000,
    systemStatus: {
      overallStatus: 'healthy',
      uptime30d: 99.97,
      services: [
        { name: 'API', status: 'healthy', latencyMs: 45, lastCheck: new Date().toISOString() },
        { name: 'Database', status: 'healthy', latencyMs: 12, lastCheck: new Date().toISOString() },
        { name: 'Oracle', status: 'degraded', latencyMs: 2300, lastCheck: new Date().toISOString() }
      ]
    },
    recentActivity: [
      { timestamp: new Date().toISOString(), adminUser: 'admin-a', actionType: 'Campaign', entityType: 'campaign', entityId: 'cmp-123', details: 'Force cancelled' },
      { timestamp: new Date().toISOString(), adminUser: 'admin-b', actionType: 'Reputation', entityType: 'creator', entityId: 'creator-456', details: 'Adjusted reputation +25' }
    ]
  }));
});

// ============ ADMIN CREATORS ============
app.get('/api/admin/creators', (req, res) => {
  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;
  const items = [
    {
      creatorId: 'creator-1',
      displayName: 'TopCreator',
      walletAddress: '0xabc123',
      status: 'active',
      reputationScore: 920,
      reputationTier: 'S',
      totalCampaigns: 78,
      totalEarnings: 125000,
      joinedAt: new Date(Date.now() - 200 * 24 * 3600 * 1000).toISOString()
    },
    {
      creatorId: 'creator-2',
      displayName: 'RisingStar',
      walletAddress: '0xdef456',
      status: 'suspended',
      reputationScore: 680,
      reputationTier: 'B',
      totalCampaigns: 32,
      totalEarnings: 38000,
      joinedAt: new Date(Date.now() - 120 * 24 * 3600 * 1000).toISOString()
    }
  ];
  res.json(apiResponse({
    items,
    page,
    size,
    total: 1240
  }));
});

app.get('/api/admin/creators/:id', (req, res) => {
  res.json(apiResponse({
    summary: {
      creatorId: req.params.id,
      displayName: 'TopCreator',
      walletAddress: '0xabc123',
      status: 'active',
      reputationScore: 920,
      reputationTier: 'S',
      totalCampaigns: 78,
      totalEarnings: 125000,
      joinedAt: new Date(Date.now() - 200 * 24 * 3600 * 1000).toISOString()
    },
    profile: {
      focusArea: [1, 2],
      languages: ['en', 'zh'],
      bio: 'DeFi + NFT specialist',
      socialAccounts: [
        { platform: 1, handle: '@topcreator', link: 'https://twitter.com/topcreator', followers: 250000, verified: true }
      ]
    },
    stats: {
      campaignsCompleted: 70,
      campaignsActive: 2,
      avgCVPI: 62.5,
      earningsTotal: 125000,
      earnings30d: 8200
    },
    recentActivity: [
      { timestamp: new Date().toISOString(), adminUser: 'admin-a', actionType: 'Status', entityType: 'creator', entityId: req.params.id, details: 'Status set to active', ipAddress: '203.0.113.5' }
    ]
  }));
});

app.post('/api/admin/creators/:id/status', (req, res) => {
  res.json(apiResponse({
    creatorId: req.params.id,
    status: req.body.status,
    reason: req.body.reason
  }));
});

app.post('/api/admin/creators/:id/reputation/adjust', (req, res) => {
  res.json(apiResponse({
    creatorId: req.params.id,
    delta: req.body.delta,
    reason: req.body.reason,
    newScore: 920 + (req.body.delta || 0)
  }));
});

app.get('/api/admin/creators/:id/campaigns', (req, res) => {
  res.json(apiResponse([
    {
      campaignId: 'cmp-1',
      name: 'DeFi Launch',
      projectName: 'ProjectX',
      status: ENUMS.CampaignStatus[3],
      budget: 25000,
      applications: 120,
      approvedApplications: 12,
      cvpi: 68.5,
      createdAt: new Date(Date.now() - 40 * 24 * 3600 * 1000).toISOString(),
      flagged: false
    }
  ]));
});

app.get('/api/admin/creators/:id/financial', (req, res) => {
  res.json(apiResponse({
    earningsTotal: 125000,
    earnings30d: 8200,
    transactions: [
      { date: new Date().toISOString(), type: 'Payment', campaign: 'cmp-1', amount: 2500, txHash: '0x123', status: 'Completed' }
    ]
  }));
});

app.get('/api/admin/creators/:id/reputation-history', (req, res) => {
  res.json(apiResponse([
    { timestamp: new Date(Date.now() - 7 * 24 * 3600 * 1000).toISOString(), score: 910, reason: 'Manual adjust +10' },
    { timestamp: new Date(Date.now() - 30 * 24 * 3600 * 1000).toISOString(), score: 900, reason: 'Campaign success' }
  ]));
});

app.get('/api/admin/creators/:id/audit-log', (req, res) => {
  res.json(apiResponse([
    { timestamp: new Date().toISOString(), adminUser: 'admin-a', actionType: 'Status', entityType: 'creator', entityId: req.params.id, details: 'Status set to active', ipAddress: '203.0.113.5' }
  ]));
});

app.get('/api/admin/reputation/overview', (req, res) => {
  res.json(apiResponse({
    averageScore: 752,
    tierCounts: { S: 24, A: 87, B: 156, C: 203, Newcomer: 142 },
    manualAdjustments30d: 12
  }));
});

app.get('/api/admin/reputation/manual-adjustments', (req, res) => {
  res.json(apiResponse([
    { delta: 25, reason: 'Outstanding delivery', effectiveAt: new Date().toISOString() },
    { delta: -40, reason: 'Policy violation', effectiveAt: new Date().toISOString() }
  ]));
});

app.put('/api/admin/reputation/tiers', (req, res) => {
  res.json(apiResponse({
    sTier: req.body.sTier ?? 900,
    aTier: req.body.aTier ?? 800,
    bTier: req.body.bTier ?? 700,
    cTier: req.body.cTier ?? 600
  }));
});

// ============ ADMIN PROJECTS ============
app.get('/api/admin/projects', (req, res) => {
  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;
  const items = [
    {
      projectId: 'project-1',
      projectName: 'ProjectX',
      walletAddress: '0xproject',
      status: 'active',
      reputationScore: 810,
      reputationTier: 'A',
      totalCampaigns: 48,
      totalBudgetSpent: 520000,
      avgCampaignCVPI: 72.3,
      joinedAt: new Date(Date.now() - 320 * 24 * 3600 * 1000).toISOString()
    }
  ];
  res.json(apiResponse({ items, page, size, total: 480 }));
});

app.get('/api/admin/projects/:id', (req, res) => {
  res.json(apiResponse({
    summary: {
      projectId: req.params.id,
      projectName: 'ProjectX',
      walletAddress: '0xproject',
      status: 'active',
      reputationScore: 810,
      reputationTier: 'A',
      totalCampaigns: 48,
      totalBudgetSpent: 520000,
      avgCampaignCVPI: 72.3,
      joinedAt: new Date(Date.now() - 320 * 24 * 3600 * 1000).toISOString()
    },
    profile: {
      bio: 'Leading DeFi infra project',
      website: 'https://projectx.io',
      socialLinks: [{ platform: 1, handle: '@projectx', link: 'https://twitter.com/projectx' }],
      verificationStatus: 'verified'
    },
    stats: {
      campaignsCreated: 48,
      budgetSpent: 520000,
      successRate: 89,
      creatorsEngaged: 340
    },
    recentActivity: [
      { timestamp: new Date().toISOString(), adminUser: 'admin-b', actionType: 'Status', entityType: 'project', entityId: req.params.id, details: 'Status checked', ipAddress: '203.0.113.5' }
    ]
  }));
});

app.post('/api/admin/projects/:id/status', (req, res) => {
  res.json(apiResponse({
    projectId: req.params.id,
    status: req.body.status,
    reason: req.body.reason
  }));
});

app.post('/api/admin/projects/:id/reputation/adjust', (req, res) => {
  res.json(apiResponse({
    projectId: req.params.id,
    delta: req.body.delta,
    reason: req.body.reason,
    newScore: 810 + (req.body.delta || 0)
  }));
});

app.get('/api/admin/projects/:id/campaigns', (req, res) => {
  res.json(apiResponse([
    {
      campaignId: 'cmp-10',
      name: 'Infrastructure Rollout',
      projectName: 'ProjectX',
      status: ENUMS.CampaignStatus[3],
      budget: 120000,
      applications: 240,
      approvedApplications: 36,
      cvpi: 82.1,
      createdAt: new Date(Date.now() - 25 * 24 * 3600 * 1000).toISOString(),
      flagged: false
    }
  ]));
});

app.get('/api/admin/projects/:id/financial', (req, res) => {
  res.json(apiResponse({
    escrowLocked: 75000,
    transactionHistory: [
      { transactionId: uuidv4(), type: 'EscrowLock', amount: 50000, token: 'USDC', status: 'Completed', timestamp: new Date().toISOString() },
      { transactionId: uuidv4(), type: 'PaymentRelease', amount: 15000, token: 'USDC', status: 'Completed', timestamp: new Date().toISOString() }
    ]
  }));
});

app.get('/api/admin/projects/:id/audit-log', (req, res) => {
  res.json(apiResponse([
    { timestamp: new Date().toISOString(), adminUser: 'admin-b', actionType: 'Status', entityType: 'project', entityId: req.params.id, details: 'Status check', ipAddress: '203.0.113.5' }
  ]));
});

// ============ ADMIN CAMPAIGNS ============
app.get('/api/admin/campaigns', (req, res) => {
  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;
  const items = [
    {
      campaignId: 'cmp-10',
      name: 'Infrastructure Rollout',
      projectName: 'ProjectX',
      status: ENUMS.CampaignStatus[3],
      budget: 120000,
      applications: 240,
      approvedApplications: 36,
      cvpi: 82.1,
      createdAt: new Date(Date.now() - 25 * 24 * 3600 * 1000).toISOString(),
      flagged: false
    }
  ];
  res.json(apiResponse({ items, page, size, total: 2875 }));
});

app.get('/api/admin/campaigns/:id', (req, res) => {
  res.json(apiResponse({
    campaignId: req.params.id,
    name: 'Infrastructure Rollout',
    projectName: 'ProjectX',
    status: ENUMS.CampaignStatus[3],
    budget: 120000,
    applications: 240,
    approvedApplications: 36,
    cvpi: 82.1,
    createdAt: new Date(Date.now() - 25 * 24 * 3600 * 1000).toISOString(),
    flagged: false
  }));
});

app.post('/api/admin/campaigns/:id/force-action', (req, res) => {
  res.json(apiResponse({
    campaignId: req.params.id,
    action: req.body.action,
    reason: req.body.reason,
    status: 'applied'
  }));
});

app.post('/api/admin/campaigns/:id/flag', (req, res) => {
  res.json(apiResponse({
    campaignId: req.params.id,
    flagged: true,
    reason: req.body.reason
  }));
});

// ============ ADMIN DISPUTES ============
app.get('/api/admin/disputes', (req, res) => {
  const page = parseInt(req.query.page) || 0;
  const size = parseInt(req.query.size) || 20;
  const items = [
    {
      disputeId: 'dsp-789',
      status: 'inArbitration',
      issueType: 'deliverable',
      campaignId: 'cmp-10',
      campaignName: 'Infrastructure Rollout',
      initiator: 'ProjectX',
      respondent: 'TopCreator',
      amount: 5000,
      filedAt: new Date(Date.now() - 3 * 24 * 3600 * 1000).toISOString(),
      escalated: true
    }
  ];
  res.json(apiResponse({ items, page, size, total: 156 }));
});

app.get('/api/admin/disputes/:id', (req, res) => {
  res.json(apiResponse({
    disputeId: req.params.id,
    status: 'inArbitration',
    issueType: 'deliverable',
    campaignId: 'cmp-10',
    campaignName: 'Infrastructure Rollout',
    initiator: 'ProjectX',
    respondent: 'TopCreator',
    amount: 5000,
    filedAt: new Date(Date.now() - 3 * 24 * 3600 * 1000).toISOString(),
    escalated: true,
    validatorsAssigned: 5,
    votes: [
      { validator: 'val-1', stakingTier: 'gold', vote: 'favorCreator', reason: 'Evidence aligns', timestamp: new Date().toISOString() }
    ],
    evidence: {
      project: ['https://example.com/evidence/project'],
      creator: ['https://example.com/evidence/creator']
    },
    activity: [
      { timestamp: new Date().toISOString(), adminUser: 'admin-c', actionType: 'Dispute', entityType: 'dispute', entityId: req.params.id, details: 'Review in progress', ipAddress: '203.0.113.10' }
    ]
  }));
});

app.post('/api/admin/disputes/:id/resolve', (req, res) => {
  res.json(apiResponse({
    disputeId: req.params.id,
    outcome: req.body.outcome,
    allocation: req.body.allocation || { favorCreatorAmount: 2500, favorProjectAmount: 2500 },
    reason: req.body.reason,
    notifyParties: req.body.notifyParties ?? true
  }));
});

// ============ ADMIN FINANCE ============
app.get('/api/admin/finance/revenue', (req, res) => {
  res.json(apiResponse({
    totalRevenue30d: 125000,
    totalCampaigns: 248,
    avgRevenuePerCampaign: 504,
    treasuryBalance: 450000,
    distribution: [
      { category: 'Treasury', percentage: 50, amount30d: 62500, status: 'Active', lastChanged: '2025-11-01' },
      { category: 'Validators', percentage: 20, amount30d: 25000, status: 'Active', lastChanged: '2025-11-01' }
    ]
  }));
});

app.get('/api/admin/finance/fees', (req, res) => {
  res.json(apiResponse({
    tiers: [
      { range: '$0 - $5,000', baseRate: 0.1, lastUpdated: '2025-11-01' },
      { range: '$5,001 - $20,000', baseRate: 0.08, lastUpdated: '2025-11-01' }
    ],
    multipliers: {
      complexity: { low: 0.8, medium: 1.0, high: 1.5 },
      reputationDiscounts: { sTier: 0.4, aTier: 0.3, bTier: 0.2, cTier: 0.1 },
      tokenDiscount: 0.2,
      oracleFee: { base: 50, perAdditionalKPI: 10 }
    }
  }));
});

app.put('/api/admin/finance/fees', (req, res) => {
  res.json(apiResponse({
    tiers: req.body.tiers || [],
    multipliers: req.body.multipliers || {}
  }));
});

// ============ ADMIN CVPI ============
app.get('/api/admin/cvpi/algorithm', (req, res) => {
  res.json(apiResponse({
    version: 'v1.2',
    effectiveDate: '2025-11-01',
    formula: 'CVPI = Total Campaign Cost / Verified Impact Score',
    components: [
      { name: 'Engagement', weight: 35, metrics: ['likes', 'comments', 'shares'], description: 'User interaction quality' },
      { name: 'Reach', weight: 25, metrics: ['impressions', 'unique views'], description: 'Content visibility' }
    ]
  }));
});

app.put('/api/admin/cvpi/algorithm', (req, res) => {
  res.json(apiResponse(req.body));
});

app.get('/api/admin/cvpi/benchmarks', (req, res) => {
  res.json(apiResponse([
    { vertical: 'DeFi', medianCVPI: 78.5, averageCVPI: 115.2, campaigns: 342, trend: -8.3 },
    { vertical: 'NFT', medianCVPI: 92.7, averageCVPI: 138.9, campaigns: 289, trend: 3.1 }
  ]));
});

app.get('/api/admin/cvpi/analytics', (req, res) => {
  res.json(apiResponse({
    totalCampaigns: 456,
    completed: 389,
    cvpiCalculated: 389,
    mean: 127.5,
    median: 85.3,
    stdDev: 78.9,
    p10: 32.5,
    p25: 52.1,
    p75: 156.8,
    p90: 284.2,
    distribution: [
      { range: '0-50', count: 142, percentage: 36.5 },
      { range: '50-100', count: 98, percentage: 25.2 }
    ]
  }));
});

// ============ ADMIN SYSTEM ============
app.get('/api/admin/system/health', (req, res) => {
  res.json(apiResponse({
    overallStatus: 'healthy',
    uptime30d: 99.97,
    services: [
      { name: 'API', status: 'healthy', latencyMs: 45, lastCheck: new Date().toISOString() },
      { name: 'Database', status: 'healthy', latencyMs: 12, lastCheck: new Date().toISOString() },
      { name: 'Oracle', status: 'degraded', latencyMs: 2300, lastCheck: new Date().toISOString() }
    ]
  }));
});

app.post('/api/admin/system/pause', (req, res) => {
  res.json(apiResponse({
    status: 'pausing',
    message: `Platform pause initiated: ${req.body.reason || 'no reason provided'}`
  }));
});

app.post('/api/admin/system/resume', (req, res) => {
  res.json(apiResponse({
    status: 'resuming',
    message: `Platform resume initiated: ${req.body.reason || 'no reason provided'}`
  }));
});

app.get('/api/admin/system/audit-logs', (req, res) => {
  res.json(apiResponse([
    { timestamp: new Date().toISOString(), adminUser: 'admin-a', actionType: 'System', entityType: 'config', entityId: 'fee-config', details: 'Updated fee tiers', ipAddress: '203.0.113.5' }
  ]));
});

// ============ ADMIN SEARCH ============
app.get('/api/admin/search', (req, res) => {
  const q = req.query.q || '';
  res.json(apiResponse([
    { type: 'creator', id: 'creator-1', name: 'TopCreator', status: 'active', relevance: 0.98 },
    { type: 'project', id: 'project-1', name: 'ProjectX', status: 'active', relevance: 0.92 },
    { type: 'campaign', id: 'cmp-10', name: 'Infrastructure Rollout', status: 'ACTIVE', relevance: 0.88 },
    { type: 'dispute', id: 'dsp-789', name: 'Dispute DSP-789', status: 'inArbitration', relevance: 0.81 }
  ].filter(item => item.name.toLowerCase().includes(q.toLowerCase()) || item.id.toLowerCase().includes(q.toLowerCase()))));
});

// ============ REQUEST DEMO API ============
// GET endpoint to retrieve list of demo requesters
app.get('/api/v1/demo-requests', async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 0;
    const size = parseInt(req.query.size) || 20;
    
    // Validate pagination parameters
    if (page < 0 || size < 1 || size > 100) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'INVALID_PARAMETERS',
          message: 'Invalid pagination parameters'
        },
        timestamp: new Date().toISOString()
      });
    }
    
    // Read all demo requests
    const allRequests = await readDemoRequests();
    
    // Sort by creation date (newest first)
    allRequests.sort((a, b) => {
      const dateA = new Date(a.createdAt).getTime();
      const dateB = new Date(b.createdAt).getTime();
      return dateB - dateA;
    });
    
    // Calculate pagination
    const totalElements = allRequests.length;
    const totalPages = Math.ceil(totalElements / size);
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedRequests = allRequests.slice(startIndex, endIndex);
    
    // Return response
    return res.status(200).json({
      success: true,
      data: {
        requesters: paginatedRequests,
        pagination: {
          currentPage: page,
          pageSize: size,
          totalElements,
          totalPages
        }
      },
      timestamp: new Date().toISOString()
    });
    
  } catch (error) {
    console.error('Error retrieving demo requests:', error);
    return res.status(500).json({
      success: false,
      error: {
        code: 'INTERNAL_ERROR',
        message: 'An unexpected error occurred. Please try again later.'
      },
      timestamp: new Date().toISOString()
    });
  }
});

// POST endpoint to submit demo request
app.post('/api/v1/demo-requests', async (req, res) => {
  try {
    const { email, userType, socialHandle, socialPlatform, source, timestamp } = req.body;
    const clientIp = req.ip || req.connection.remoteAddress || req.headers['x-forwarded-for'] || 'unknown';
    
    // Validation errors array
    const validationErrors = [];
    
    // Validate required fields
    if (!email) {
      validationErrors.push({ field: 'email', message: 'Email is required' });
    } else if (!validateEmail(email)) {
      validationErrors.push({ field: 'email', message: 'Invalid email format' });
    }
    
    if (!userType) {
      validationErrors.push({ field: 'userType', message: 'User type is required' });
    } else {
      const normalized = normalizeUserType(userType);
      if (!normalized) {
        validationErrors.push({ field: 'userType', message: 'User type must be either "creator" or "project_owner"' });
      }
    }
    
    if (!socialHandle) {
      validationErrors.push({ field: 'socialHandle', message: 'Social handle is required' });
    } else if (!validateSocialHandle(socialHandle)) {
      validationErrors.push({ field: 'socialHandle', message: 'Social handle must be 3-50 characters, alphanumeric with underscores and hyphens' });
    }
    
    if (!socialPlatform) {
      validationErrors.push({ field: 'socialPlatform', message: 'Social platform is required' });
    } else {
      const normalized = normalizePlatform(socialPlatform);
      if (!normalized) {
        validationErrors.push({ field: 'socialPlatform', message: 'Social platform must be either "telegram" or "x"' });
      }
    }
    
    // Validate optional fields
    if (source && typeof source === 'string' && source.length > 100) {
      validationErrors.push({ field: 'source', message: 'Source must be maximum 100 characters' });
    }
    
    // Return validation errors if any
    if (validationErrors.length > 0) {
      return res.status(400).json({
        success: false,
        error: {
          code: 'VALIDATION_ERROR',
          message: 'Invalid request data',
          details: validationErrors
        },
        timestamp: new Date().toISOString()
      });
    }
    
    // Normalize values
    const normalizedEmail = normalizeEmail(email);
    const normalizedUserType = normalizeUserType(userType);
    const normalizedPlatform = normalizePlatform(socialPlatform);
    const cleanedHandle = stripAtSymbol(socialHandle);
    
    // Check rate limits
    const rateLimitCheck = checkRateLimit(clientIp, normalizedEmail);
    if (rateLimitCheck.limited) {
      return res.status(429).json({
        success: false,
        error: {
          code: 'RATE_LIMIT_EXCEEDED',
          message: 'Too many requests. Please try again later.',
          details: {
            retryAfter: rateLimitCheck.retryAfter
          }
        },
        timestamp: new Date().toISOString()
      });
    }
    
    // Check for duplicates
    const duplicate = await checkDuplicate(normalizedEmail);
    if (duplicate) {
      return res.status(409).json({
        success: false,
        error: {
          code: 'DUPLICATE_REQUEST',
          message: 'A demo request with this email already exists',
          details: {
            existingRequestId: duplicate.requestId,
            submittedAt: duplicate.createdAt
          }
        },
        timestamp: new Date().toISOString()
      });
    }
    
    // Generate request ID and prepare data
    const requestId = generateRequestId();
    const createdAt = new Date().toISOString();
    const requestData = {
      requestId,
      email: normalizedEmail,
      userType: normalizedUserType,
      socialHandle: cleanedHandle,
      socialPlatform: normalizedPlatform,
      source: source || null,
      timestamp: timestamp || Date.now(),
      ipAddress: clientIp,
      createdAt
    };
    
    // Try to write to file (will fail in serverless, but that's OK for mock API)
    try {
      await writeDemoRequest(requestData);
    } catch (writeError) {
      // In serverless environments like Vercel, file writes will fail
      // Log the error but don't fail the request since this is a mock API
      console.log('Note: File write skipped (serverless environment):', writeError.message);
    }
    
    // Return success response
    return res.status(201).json({
      success: true,
      data: {
        requestId,
        email: normalizedEmail,
        userType: normalizedUserType,
        status: 'pending',
        createdAt
      },
      message: 'Demo request submitted successfully. We will contact you soon.',
      timestamp: new Date().toISOString()
    });
    
  } catch (error) {
    console.error('Error processing demo request:', error);
    return res.status(500).json({
      success: false,
      error: {
        code: 'INTERNAL_ERROR',
        message: 'An unexpected error occurred. Please try again later.'
      },
      timestamp: new Date().toISOString()
    });
  }
});

// 404 handler
app.use((req, res) => {
  res.status(404).json(apiError('NOT_FOUND', `Endpoint ${req.path} not found`));
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json(apiError('INTERNAL_ERROR', 'Internal server error'));
});

// Start server (for Railway, Render, local development)
if (process.env.VERCEL !== '1') {
  app.listen(PORT, () => {
    console.log(`AW3 Platform Mock API running on port ${PORT}`);
    console.log(`Swagger UI: http://localhost:${PORT}/docs`);
    console.log(`Health check: http://localhost:${PORT}/health`);
  });
}

// Export for Vercel serverless
module.exports = app;
