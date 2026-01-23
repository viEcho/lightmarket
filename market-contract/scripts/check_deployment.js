const hre = require("hardhat");

async function main() {
  console.log("🔍 检查合约部署状态...\n");

  // 合约地址
  const factoryAddress = "0x9fE46736679d2D9a65F0992F2272dE9f3c7fa6e0";
  const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

  // 获取签名者
  const [signer] = await hre.ethers.getSigners();
  console.log("👤 当前账户:", signer.address);
  console.log("   账户余额:", hre.ethers.formatEther(await hre.ethers.provider.getBalance(signer.address)), "ETH\n");

  try {
    // 1. 检查 USDC 合约
    console.log("1️⃣ 检查 USDC 合约...");
    const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);
    const usdcName = await usdc.name();
    const usdcSymbol = await usdc.symbol();
    const usdcDecimals = await usdc.decimals();
    const usdcBalance = await usdc.balanceOf(signer.address);

    console.log("✅ USDC 合约正常");
    console.log("   名称:", usdcName);
    console.log("   符号:", usdcSymbol);
    console.log("   小数位:", usdcDecimals);
    console.log("   余额:", hre.ethers.formatUnits(usdcBalance, 6), "USDC\n");

    // 2. 检查 Factory 合约
    console.log("2️⃣ 检查 Factory 合约...");
    const factory = await hre.ethers.getContractAt("MarketFactory", factoryAddress);
    const factoryUSDC = await factory.usdc();

    console.log("✅ Factory 合约正常");
    console.log("   地址:", factoryAddress);
    console.log("   USDC 地址:", factoryUSDC);

    if (factoryUSDC.toLowerCase() !== usdcAddress.toLowerCase()) {
      console.log("⚠️  警告: Factory 的 USDC 地址不匹配!");
    } else {
      console.log("✅ USDC 地址匹配\n");
    }

    // 3. 检查当前市场数量
    console.log("3️⃣ 检查已创建的市场...");
    const marketCount = await factory.marketCount();
    console.log("   已创建市场数量:", marketCount.toString());

    if (marketCount > 0) {
      console.log("\n📋 已创建的市场列表:");
      for (let i = 0; i < marketCount; i++) {
        try {
          const marketId = await factory.marketIds(i);
          const marketAddress = await factory.getMarketAddress(marketId);
          const exists = await factory.marketExists(marketId);

          console.log(`\n   市场 #${i + 1}:`);
          console.log("   - Market ID:", marketId);
          console.log("   - 合约地址:", marketAddress);
          console.log("   - 是否存在:", exists ? "✅" : "❌");

          // 如果市场存在，尝试获取更多信息
          if (exists) {
            try {
              const market = await hre.ethers.getContractAt("OrderbookMarket", marketAddress);
              const endTime = await market.endTime();
              const initialLiquidity = await market.initialLiquidity();

              console.log("   - 结束时间:", new Date(Number(endTime) * 1000).toLocaleString());
              console.log("   - 初始流动性:", hre.ethers.formatUnits(initialLiquidity, 6), "USDC");
            } catch (err) {
              console.log("   - 无法获取市场详细信息:", err.message);
            }
          }
        } catch (err) {
          console.log(`   无法获取市场 #${i + 1} 信息:`, err.message);
        }
      }
    } else {
      console.log("   还没有创建任何市场\n");
    }

    // 4. 检查授权状态
    console.log("4️⃣ 检查 USDC 授权状态...");
    const allowance = await usdc.allowance(signer.address, factoryAddress);
    console.log("   当前授权额度:", hre.ethers.formatUnits(allowance, 6), "USDC");

    if (allowance === 0n) {
      console.log("⚠️  未授权! 需要先授权 Factory 合约才能使用 USDC");
      console.log("\n💡 授权命令 (在 Hardhat console 中):");
      console.log(`   const usdc = await ethers.getContractAt("MockUSDC", "${usdcAddress}");`);
      console.log(`   const tx = await usdc.approve("${factoryAddress}", ethers.parseUnits("1000000", 6));`);
      console.log(`   await tx.wait();`);
    } else {
      console.log("✅ 已授权\n");
    }

    console.log("\n✅ 所有合约检查完成!");

  } catch (error) {
    console.error("\n❌ 检查失败:", error.message);
    console.error("\n可能的原因:");
    console.error("1. Hardhat 节点没有运行");
    console.error("2. 合约地址错误");
    console.error("3. 合约未正确部署");

    process.exit(1);
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
