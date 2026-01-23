#!/bin/bash

echo "🚀 Starting Hardhat Node and Deploying Contracts..."
echo ""

# 启动 hardhat 节点(后台运行)
echo "📡 Starting Hardhat node in background..."
npx hardhat node > /tmp/hardhat-node.log 2>&1 &
HARDHAT_PID=$!
echo "✅ Hardhat node started (PID: $HARDHAT_PID)"
echo "   Log file: /tmp/hardhat-node.log"

# 等待节点启动
echo "⏳ Waiting for node to start..."
sleep 5

# 部署 USDC
echo ""
echo "📦 Deploying MockUSDC..."
npx hardhat run scripts/deploy_usdc.js --network localhost

echo ""
echo "✅ Setup complete!"
echo ""
echo "📝 IMPORTANT:"
echo "   1. Hardhat node is running in background (PID: $HARDHAT_PID)"
echo "   2. Update your .env file with the USDC address above"
echo "   3. Keep the Hardhat node running while testing"
echo ""
echo "🛑 To stop the node, run: kill $HARDHAT_PID"
echo ""
echo "💡 Tip: You can view logs with: tail -f /tmp/hardhat-node.log"
