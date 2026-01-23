const hre = require("hardhat");

async function main() {
  const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";

  // 获取所有测试账户
  const signers = await hre.ethers.getSigners();

  console.log("\n===== USDC 余额查询 =====\n");

  const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);

  for (let i = 0; i < Math.min(5, signers.length); i++) {
    const signer = signers[i];
    const balance = await usdc.balanceOf(signer.address);
    console.log(`账户 #${i}: ${signer.address}`);
    console.log(`余额: ${hre.ethers.formatUnits(balance, 6)} USDC\n`);
  }

  console.log("======================\n");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
