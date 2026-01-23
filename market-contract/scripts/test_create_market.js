const hre = require("hardhat");

async function main() {
  console.log("🧪 测试创建市场...\n");

  // 合约地址
  const factoryAddress = "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0";
  const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

  const [signer] = await hre.ethers.getSigners();
  console.log("👤 创建者账户:", signer.address);

  try {
    // 1. 获取合约实例
    const factory = await hre.ethers.getContractAt("MarketFactory", factoryAddress);
    const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);

    // 2. 检查当前余额
    const balance = await usdc.balanceOf(signer.address);
    console.log("💰 当前 USDC 余额:", hre.ethers.formatUnits(balance, 6), "USDC");

    // 3. 先授权
    console.log("\n📝 步骤 1: 授权 USDC 给 Factory...");
    const approveAmount = hre.ethers.parseUnits("10000", 6); // 授权 10000 USDC

    const approveTx = await usdc.approve(factoryAddress, approveAmount);
    console.log("   交易哈希:", approveTx.hash);
    console.log("   等待确认...");
    await approveTx.wait();
    console.log("✅ 授权成功!");

    // 验证授权
    const allowance = await usdc.allowance(signer.address, factoryAddress);
    console.log("   授权额度:", hre.ethers.formatUnits(allowance, 6), "USDC\n");

    // 4. 创建市场
    console.log("📦 步骤 2: 创建市场...");

    // 生成市场 ID
    const marketId = hre.ethers.keccak256(hre.ethers.toUtf8Bytes("test-market-" + Date.now()));

    // 设置结束时间 (1小时后)
    const endTime = Math.floor(Date.now() / 1000) + 3600;

    // 初始流动性 (1000 USDC)
    const initialLiquidity = hre.ethers.parseUnits("1000", 6);

    console.log("   Market ID:", marketId);
    console.log("   结束时间:", new Date(endTime * 1000).toLocaleString());
    console.log("   初始流动性:", hre.ethers.formatUnits(initialLiquidity, 6), "USDC");

    // 估算 gas
    console.log("\n⏳ 估算 Gas...");
    const gasEstimate = await factory.createMarket.estimateGas(marketId, endTime, initialLiquidity);
    console.log("   预估 Gas:", gasEstimate.toString());

    // 发送交易
    console.log("\n🚀 发送创建市场交易...");
    const tx = await factory.createMarket(marketId, endTime, initialLiquidity, {
      gasLimit: gasEstimate * 12n / 10n // 增加 20%
    });

    console.log("   交易哈希:", tx.hash);
    console.log("   等待确认...");

    const receipt = await tx.wait();
    console.log("✅ 交易已确认!");
    console.log("   Gas 使用:", receipt.gasUsed.toString());
    console.log("   区块号:", receipt.blockNumber);

    // 5. 检查创建的市场
    console.log("\n🔍 验证市场创建...");

    const marketCount = await factory.marketCount();
    console.log("   市场总数:", marketCount.toString());

    const marketAddress = await factory.getMarketAddress(marketId);
    console.log("   市场合约地址:", marketAddress);

    const exists = await factory.marketExists(marketId);
    console.log("   市场存在:", exists ? "✅" : "❌");

    // 6. 获取市场详情
    if (exists) {
      console.log("\n📊 市场详情:");
      const market = await hre.ethers.getContractAt("OrderbookMarket", marketAddress);

      const marketEndTime = await market.endTime();
      const liquidity = await market.initialLiquidity();
      const creator = await market.creator();

      console.log("   创建者:", creator);
      console.log("   结束时间:", new Date(Number(marketEndTime) * 1000).toLocaleString());
      console.log("   初始流动性:", hre.ethers.formatUnits(liquidity, 6), "USDC");
    }

    // 7. 检查 USDC 余额变化
    const newBalance = await usdc.balanceOf(signer.address);
    console.log("\n💰 交易后余额:", hre.ethers.formatUnits(newBalance, 6), "USDC");
    console.log("   消耗:", hre.ethers.formatUnits(balance - newBalance, 6), "USDC");

    console.log("\n✅ 市场创建测试成功!");

  } catch (error) {
    console.error("\n❌ 测试失败:", error.message);

    if (error.message.includes("insufficient")) {
      console.error("   原因: 余额不足");
    } else if (error.message.includes("allowance")) {
      console.error("   原因: 授权额度不足");
    } else if (error.message.includes("revert")) {
      console.error("   原因: 合约执行失败");
    }

    process.exit(1);
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
