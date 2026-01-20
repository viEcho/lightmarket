# Wallet Connection Setup Guide

This document explains how to set up and use the wallet connection functionality in LightMarket.

## Features Implemented

### Frontend (Vue 3)

1. **User Store with Pinia** (`market-web/src/stores/user.js`)
   - Wallet connection state management
   - User authentication
   - Auto-registration for new users
   - Persistent login with localStorage

2. **Header Wallet UI** (`market-web/src/components/Header.vue`)
   - Connect Wallet button
   - Display connected wallet address (shortened format)
   - Show wallet balance
   - Dropdown menu with wallet details
   - Disconnect wallet functionality

3. **Market Detail Integration** (`market-web/src/components/MarketDetail.vue`)
   - Wallet connection prompt when clicking YES/NO buttons
   - Auto-connect workflow
   - Trading requires wallet connection

### Backend (Spring Boot)

1. **User Entity** (`market-backend/src/main/java/com/market/business/entity/User.java`)
   - Wallet address
   - Balance tracking
   - Trade statistics
   - Account status

2. **User API** (`market-backend/src/main/java/com/market/business/controller/UserController.java`)
   - `POST /api/user/check` - Check if user exists
   - `POST /api/user/register` - Register new user
   - `GET /api/user/info/{walletAddress}` - Get user info
   - `POST /api/user/balance/update` - Update user balance

3. **Database Schema** (`market-backend/sql/user_table.sql`)
   - MySQL user table with all necessary fields

## Setup Instructions

### 1. Database Setup

```bash
# Connect to your MySQL database
mysql -u root -p

# Execute the SQL script
source market-backend/sql/user_table.sql
```

### 2. Backend Configuration

Update `market-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lightmarket
    username: your_username
    password: your_password
```

### 3. Start Backend Server

```bash
cd market-backend
mvn spring-boot:run
```

The backend will run on `http://localhost:9999`

### 4. Start Frontend Server

```bash
cd market-web
npm install  # Already done if you followed the steps
npm run dev
```

The frontend will run on `http://localhost:5173`

## Usage Flow

### First Time User

1. User opens the market page
2. User clicks YES or NO button
3. System prompts: "You need to connect your wallet to trade..."
4. User clicks OK
5. MetaMask popup appears
6. User approves connection
7. Frontend calls `/api/user/check` - User not found
8. Frontend calls `/api/user/register` - New user created
9. User can now trade

### Returning User

1. User opens the market page
2. System checks localStorage for saved wallet
3. If found, auto-connects to wallet
4. Frontend calls `/api/user/check` - User found
5. Frontend calls `/api/user/info/{walletAddress}` - Load user data
6. User can trade immediately

## API Endpoints

### Check User

```http
POST http://localhost:9999/api/user/check
Content-Type: application/json

{
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678"
}
```

Response:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "exists": true
  }
}
```

### Register User

```http
POST http://localhost:9999/api/user/register
Content-Type: application/json

{
  "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
  "username": "optional_username",
  "email": "optional@email.com"
}
```

Response:
```json
{
  "code": 200,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "walletAddress": "0x1234567890abcdef1234567890abcdef12345678",
    "balance": 0.0,
    "status": "active",
    "createdAt": "2025-01-20T10:30:00"
  }
}
```

## Dependencies Installed

### Frontend
- `ethers@6` - Ethereum wallet interaction
- `pinia` - State management

### Backend
- MyBatis Plus - Already included
- Lombok - Already included

## Security Considerations

1. **CORS Configuration**: Make sure your backend has CORS enabled for the frontend URL
2. **Wallet Validation**: Always validate wallet addresses on the backend
3. **Rate Limiting**: Implement rate limiting for API endpoints
4. **HTTPS**: Use HTTPS in production

## Troubleshooting

### MetaMask Not Detected

- Make sure MetaMask is installed
- Refresh the page after installing MetaMask
- Check browser console for errors

### Backend Connection Errors

- Verify backend is running on port 9999
- Check CORS configuration
- Verify MySQL database is running

### Wallet Connection Lost

- Connection state is saved in localStorage
- User can reconnect by clicking "Connect Wallet" button
- Check MetaMask is unlocked

## Next Steps

1. Implement actual trading functionality
2. Add transaction signing
3. Implement order submission
4. Add portfolio tracking
5. Implement transaction history
