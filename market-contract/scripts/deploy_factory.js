const hre = require("hardhat");

async function main() {
  // 获取USDC地址
  const usdcAddress = process.env.USDC_ADDRESS;
  if (!usdcAddress) {
    console.error("Please set USDC_ADDRESS in .env file");
    process.exit(1);
  }

  console.log("Deploying MarketFactory...");
  console.log("USDC Address:", usdcAddress);

  const MarketFactory = await hre.ethers.getContractFactory("MarketFactory");
  const factory = await MarketFactory.deploy(usdcAddress);

  await factory.waitForDeployment();
  const factoryAddress = await factory.getAddress();

  console.log("MarketFactory deployed to:", factoryAddress);
  console.log("\nAdd this to your .env file:");
  console.log(`FACTORY_ADDRESS=${factoryAddress}`);

  console.log("\nDeployment completed!");
  console.log("\nVerification command:");
  console.log(`npx hardhat verify --network ${hre.network.name} ${factoryAddress} ${usdcAddress}`);
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
