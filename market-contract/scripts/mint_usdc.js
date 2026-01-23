const hre = require("hardhat");

/**
 * 铸造测试 USDC
 *
 * 用法:
 *   USDC_ADDRESS=0x... TO_ADDRESS=0x... AMOUNT=10000 npx hardhat run scripts/mint_usdc.js
 *
 * 环境变量:
 *   USDC_ADDRESS - USDC 合约地址
 *   TO_ADDRESS - 接收地址（可选，默认为第一个签名者）
 *   AMOUNT - 铸造数量（可选，默认 10000）
 */
async function main() {
  // 获取环境变量
  const usdcAddress = process.env.USDC_ADDRESS;
  if (!usdcAddress) {
    console.error("❌ 请设置 USDC_ADDRESS 环境变量");
    console.error("示例: USDC_ADDRESS=0x... npx hardhat run scripts/mint_usdc.js");
    process.exit(1);
  }

  const toAddress = process.env.TO_ADDRESS;
  const amount = process.env.AMOUNT || "10000";

  console.log("\n========================================");
  console.log("🪙 铸造测试 USDC");
  console.log("========================================");
  console.log("USDC 合约地址:", usdcAddress);
  console.log("铸造数量:", amount);
  console.log("========================================\n");

  // 获取签名者
  const [signer] = await hre.ethers.getSigners();
  const recipient = toAddress || signer.address;

  console.log("发送账户:", signer.address);
  console.log("接收账户:", recipient);

  // 获取 USDC 合约
  const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);

  // 铸造前余额
  const balanceBefore = await usdc.balanceOf(recipient);
  console.log("\n铸造前余额:", hre.ethers.formatUnits(balanceBefore, 6), "USDC");

  // 铸造 USDC
  console.log("\n⏳ 正在铸造...");
  const amountWei = hre.ethers.parseUnits(amount, 6);
  const tx = await usdc.mint(recipient, amountWei);
  console.log("✅ 交易已提交:", tx.hash);

  // 等待确认
  const receipt = await tx.wait();
  console.log("✅ 交易已确认");
  console.log("Gas 使用:", receipt.gasUsed.toString());

  // 铸造后余额
  const balanceAfter = await usdc.balanceOf(recipient);
  console.log("\n铸造后余额:", hre.ethers.formatUnits(balanceAfter, 6), "USDC");
  console.log("增加数量:", hre.ethers.formatUnits(balanceAfter - balanceBefore, 6), "USDC");

  console.log("\n========================================");
  console.log("🎉 铸造成功!");
  console.log("========================================\n");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error("\n❌ 错误:", error.message);
    process.exit(1);
  });
