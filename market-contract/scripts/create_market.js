const hre = require("hardhat");

async function main() {
  // 获取环境变量
  const factoryAddress = process.env.FACTORY_ADDRESS;
  if (!factoryAddress) {
    console.error("Please set FACTORY_ADDRESS in .env file");
    process.exit(1);
  }

  console.log("Creating market...");
  console.log("Factory Address:", factoryAddress);

  const [signer] = await hre.ethers.getSigners();
  console.log("Creating market from account:", signer.address);

  // 获取Factory合约
  const factory = await hre.ethers.getContractAt("MarketFactory", factoryAddress);

  // 获取USDC合约
  const usdcAddress = await factory.usdc();
  const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);

  // 市场参数
  const marketId = hre.ethers.keccak256(hre.ethers.toUtf8Bytes("market-1")); // 示例marketId
  const endTime = Math.floor(Date.now() / 1000) + 7 * 24 * 60 * 60; // 7天后结束
  const initialLiquidity = hre.ethers.parseUnits("100", 6); // 100 USDC

  console.log("\nMarket Parameters:");
  console.log("Market ID:", marketId);
  console.log("End Time:", new Date(endTime * 1000).toISOString());
  console.log("Initial Liquidity:", hre.ethers.formatUnits(initialLiquidity, 6), "USDC");

  // 1️⃣ Approve USDC给Factory
  console.log("\n1. Approving USDC to Factory...");
  const approveTx = await usdc.approve(factoryAddress, initialLiquidity);
  await approveTx.wait();
  console.log("Approved!");

  // 2️⃣ 创建市场
  console.log("\n2. Creating market...");
  const createTx = await factory.createMarket(marketId, endTime, initialLiquidity);
  const receipt = await createTx.wait();

  // 3️⃣ 从事件中获取Market地址
  const event = receipt.logs.find(log => {
    try {
      const parsed = factory.interface.parseLog(log);
      return parsed.name === "MarketCreated";
    } catch (e) {
      return false;
    }
  });

  if (event) {
    const parsed = factory.interface.parseLog(event);
    console.log("\nMarket created successfully!");
    console.log("Market Address:", parsed.args.market);
    console.log("Market ID:", parsed.args.marketId);
    console.log("Creator:", parsed.args.creator);
    console.log("End Time:", new Date(Number(parsed.args.endTime) * 1000).toISOString());
    console.log("Initial Liquidity:", hre.ethers.formatUnits(parsed.args.initialLiquidity, 6), "USDC");
  }

  console.log("\nCompleted!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
