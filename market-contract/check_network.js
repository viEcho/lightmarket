const hre = require("hardhat");

async function main() {
  const network = await hre.ethers.provider.getNetwork();
  console.log("Hardhat 网络信息:");
  console.log("  Chain ID:", network.chainId.toString());
  console.log("  Network Name:", network.name);
}

main();
