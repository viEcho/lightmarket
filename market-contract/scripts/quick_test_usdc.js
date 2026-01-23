const hre = require("hardhat");

async function main() {
  console.log("🧪 Quick Testing USDC Contract...\n");

  // 获取签名者
  const [signer] = await hre.ethers.getSigners();
  console.log("👤 Testing from account:", signer.address);

  try {
    // 部署 USDC 合约
    console.log("\n📦 Deploying MockUSDC...");
    const MockUSDC = await hre.ethers.getContractFactory("MockUSDC");
    const usdc = await MockUSDC.deploy();
    await usdc.waitForDeployment();
    const usdcAddress = await usdc.getAddress();

    console.log("✅ MockUSDC deployed to:", usdcAddress);
    console.log("\n⚠️  UPDATE YOUR .env FILE:");
    console.log(`   VITE_USDC_ADDRESS=${usdcAddress}`);

    // 铸造代币
    console.log("\n💰 Minting 1,000,000 USDC to deployer...");
    const mintAmount = hre.ethers.parseUnits("1000000", 6);
    const mintTx = await usdc.mint(signer.address, mintAmount);
    await mintTx.wait();
    console.log("✅ Minted successfully!");

    // 检查余额
    const balance = await usdc.balanceOf(signer.address);
    console.log("\n💵 Current balance:", hre.ethers.formatUnits(balance, 6), "USDC");

    // 测试 transfer
    console.log("\n🔄 Testing transfer...");
    const [_, receiver] = await hre.ethers.getSigners();
    const transferAmount = hre.ethers.parseUnits("100", 6);
    const transferTx = await usdc.transfer(receiver.address, transferAmount);
    await transferTx.wait();

    const receiverBalance = await usdc.balanceOf(receiver.address);
    console.log("✅ Transferred 100 USDC to", receiver.address);
    console.log("   Receiver balance:", hre.ethers.formatUnits(receiverBalance, 6), "USDC");

    console.log("\n✅ All tests passed!");

  } catch (error) {
    console.error("\n❌ Test failed:", error.message);
    throw error;
  }
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
