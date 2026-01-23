const hre = require("hardhat");

async function main() {
  console.log("🔍 验证已创建的市场...\n");

  const factoryAddress = "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0";
  const [signer] = await hre.ethers.getSigners();

  const factory = await hre.ethers.getContractAt("MarketFactory", factoryAddress);

  // 获取市场数量
  const marketCount = await factory.marketCount();
  console.log("📊 市场总数:", marketCount.toString());

  if (marketCount.toString() === "0") {
    console.log("   还没有创建任何市场");
    return;
  }

  // 获取所有市场ID
  const allMarketIds = await factory.getAllMarketIds();
  const marketId = allMarketIds[0];
  const marketAddress = await factory.getMarketAddress(marketId);

  console.log("\n🎯 第一个市场:");
  console.log("   Market ID:", marketId);
  console.log("   合约地址:", marketAddress);

  // 尝试使用 PredictionMarket 合约
  try {
    console.log("\n📜 使用 PredictionMarket 合约接口...");
    const market = await hre.ethers.getContractAt("PredictionMarket", marketAddress);

    const creator = await market.creator();
    const endTime = await market.endTime();
    const unlockTime = await market.unlockTime();
    const marketId = await market.marketId();
    const usdc = await market.usdc();

    console.log("✅ 合约类型: PredictionMarket");
    console.log("   创建者:", creator);
    console.log("   USDC 地址:", usdc);
    console.log("   Market ID:", marketId);
    console.log("   结束时间:", new Date(Number(endTime) * 1000).toLocaleString());
    console.log("   解锁时间:", new Date(Number(unlockTime) * 1000).toLocaleString());

    // 检查池子信息
    const yesPool = await market.yesPool();
    const noPool = await market.noPool();

    console.log("\n💧 池子信息:");
    console.log("   YES Pool:", hre.ethers.formatUnits(yesPool, 6), "USDC");
    console.log("   NO Pool:", hre.ethers.formatUnits(noPool, 6), "USDC");

    // 计算价格
    if (yesPool > 0n || noPool > 0n) {
      const yesPrice = noPool > 0n
        ? (yesPool * 1000000n) / (yesPool + noPool)
        : 1000000n;
      const noPrice = yesPool > 0n
        ? (noPool * 1000000n) / (yesPool + noPool)
        : 1000000n;

      console.log("\n💰 当前价格:");
      console.log("   YES 价格:", Number(yesPrice) / 10000, "¢");
      console.log("   NO 价格:", Number(noPrice) / 10000, "¢");
    }

  } catch (error) {
    console.log("❌ 无法读取市场信息:", error.message);
  }

  console.log("\n✅ 市场创建验证完成!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
