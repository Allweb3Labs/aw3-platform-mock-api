# AW3 Platform Mock API

A comprehensive mock API for the AW3 (Allweb3) Platform with Swagger/OpenAPI documentation.

## Quick Start

### Local Development

```bash
cd swagger-mock-api
npm install
npm start
```

Server will start at `http://localhost:3000`

### Access Points

| Endpoint | Description |
|----------|-------------|
| `/` | API info and available endpoints |
| `/docs` | Swagger UI Documentation |
| `/swagger.yaml` | OpenAPI 3.0 specification (YAML) |
| `/swagger.json` | OpenAPI 3.0 specification (JSON) |
| `/health` | Health check endpoint |

## API Overview

### Authentication (`/api/auth/*`)
- Wallet-based authentication using DID
- JWT token management
- User registration with role selection

### Creator Portal (`/api/creator/*`)
- Profile management
- Campaign discovery & applications
- Deliverable submission
- CVPI score tracking
- Earnings management

### Project Portal (`/api/project/*`)
- Profile management
- Campaign creation & management
- Application review
- Fee estimation & escrow

### Admin Portal (`/api/admin/*`)
- Campaign administration
- User management
- Analytics & reporting

### Public Marketplace (`/api/public/*`)
- Public campaign browsing
- Creator & project discovery
- Platform statistics

## Authentication

Most endpoints require JWT authentication. Use the Bearer token scheme:

```
Authorization: Bearer <your_jwt_token>
```

To get a token:
1. POST `/api/auth/wallet-connect` with wallet address
2. Sign the returned message with your wallet
3. POST `/api/auth/verify-signature` with signature
4. Use the returned `accessToken`

## Example Requests

### Get Nonce for Wallet
```bash
curl -X POST http://localhost:3000/api/auth/wallet-connect \
  -H "Content-Type: application/json" \
  -d '{"walletAddress": "0x742d35Cc6634C0532925a3b844Bc454e4438f44e"}'
```

### Browse Campaigns
```bash
curl http://localhost:3000/api/public/marketplace/campaigns
```

### Get Platform Stats
```bash
curl http://localhost:3000/api/public/marketplace/stats
```

## Deploy to Render

1. Push this folder to a GitHub repository
2. Go to [render.com](https://render.com)
3. Create a new "Web Service"
4. Connect your GitHub repository
5. Configure:
   - **Name**: aw3-api-mock
   - **Runtime**: Node
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
6. Deploy!

## Available Mock Data

The API includes realistic mock data for:
- 3 sample campaigns (DeFi, NFT, GameFi)
- Creator profile with social verification
- Applications and deliverables
- CVPI scores and history
- Earnings transactions
- Platform statistics

## Tech Stack

- Node.js + Express
- swagger-ui-express for documentation
- YAML.js for OpenAPI parsing
- CORS enabled for cross-origin requests

## License

MIT

