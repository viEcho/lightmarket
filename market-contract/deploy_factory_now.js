const hre = require("hardhat");

async function main() {
  console.log("Deploying MarketFactory...");

  const USDC_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  
  const Factory = await hre.ethers.getContractFactory("MarketFactory");
  const factory = await Factory.deploy(USDC_ADDRESS);

  await factory.waitForDeployment();
  const factoryAddress = await factory.getAddress();

  console.log("MarketFactory deployed to:", factoryAddress);
  console.log("\nAdd this to your .env file:");
  console.log(`VITE_FACTORY_ADDRESS=${factoryAddress}`);
  console.log(`VITE_USDC_ADDRESS=${USDC_ADDRESS}`);
  console.log("\nDeployment completed!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
