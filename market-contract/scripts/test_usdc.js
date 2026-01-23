const hre = require("hardhat");

async function main() {
  console.log("Testing USDC Contract...\n");

  // USDC 合约地址
  const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  console.log("USDC Address:", usdcAddress);

  // 获取签名者
  const [signer] = await hre.ethers.getSigners();
  console.log("Testing from account:", signer.address);

  // 获取合约
  const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);

  // 检查初始余额
  const balance = await usdc.balanceOf(signer.address);
  console.log("\nCurrent balance:", hre.ethers.formatUnits(balance, 6), "USDC");

  // 测试铸造
  console.log("\nMinting 100 USDC...");
  const mintAmount = hre.ethers.parseUnits("100", 6);

  const tx = await usdc.mint(signer.address, mintAmount);
  console.log("Transaction hash:", tx.hash);

  console.log("Waiting for confirmation...");
  const receipt = await tx.wait();
  console.log("Transaction confirmed in block:", receipt.blockNumber);

  // 检查新余额
  const newBalance = await usdc.balanceOf(signer.address);
  console.log("\nNew balance:", hre.ethers.formatUnits(newBalance, 6), "USDC");
  console.log("Increase:", hre.ethers.formatUnits(newBalance - balance, 6), "USDC");

  console.log("\n✅ Test completed successfully!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
