const hre = require("hardhat");

async function main() {
  console.log("Deploying MockUSDC...");

  const MockUSDC = await hre.ethers.getContractFactory("MockUSDC");
  const usdc = await MockUSDC.deploy();

  await usdc.waitForDeployment();
  const usdcAddress = await usdc.getAddress();

  console.log("MockUSDC deployed to:", usdcAddress);
  console.log("\nAdd this to your .env file:");
  console.log(`USDC_ADDRESS=${usdcAddress}`);

  // 铸造一些测试代币给部署者
  const [deployer] = await hre.ethers.getSigners();
  const mintAmount = hre.ethers.parseUnits("1000000", 6); // 100万 USDC
  await usdc.mint(deployer.address, mintAmount);
  console.log(`\nMinted ${hre.ethers.formatUnits(mintAmount, 6)} USDC to deployer:`, deployer.address);

  console.log("\nDeployment completed!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
