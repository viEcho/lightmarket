const hre = require("hardhat");

/**
 * 批量铸造 USDC 给所有测试用户
 * 用途：快速初始化所有测试用户的 USDC 余额
 */

async function main() {
  console.log("\n===== 批量铸造 USDC =====\n");

  const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  const mintAmount = hre.ethers.parseUnits("10000", 6); // 每个 10,000 USDC

  // 测试用户钱包地址列表
  const testUsers = [
    { name: "alice", address: "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266" },
    { name: "bob", address: "0x70997970C51812dc3A010C7d01b50e0d17dc79C8" },
    { name: "charlie", address: "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC" },
    { name: "david", address: "0x90F79bf6EB2c4f870365E785982E1f101E93b906" },
    { name: "eve", address: "0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65" },
    { name: "alice_wallet2", address: "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc" }
  ];

  // 获取合约
  const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);
  const [signer] = await hre.ethers.getSigners();

  console.log("部署者地址:", signer.address);
  console.log("铸造数量:", hre.ethers.formatUnits(mintAmount, 6), "USDC/用户\n");

  // 批量铸造
  for (const user of testUsers) {
    try {
      console.log(`正在铸造给 ${user.name} (${user.address})...`);

      // 查询当前余额
      const balanceBefore = await usdc.balanceOf(user.address);

      // 铸造
      const tx = await usdc.mint(user.address, mintAmount);
      await tx.wait();

      // 查询新余额
      const balanceAfter = await usdc.balanceOf(user.address);

      console.log(`  ✅ 成功! 余额: ${hre.ethers.formatUnits(balanceBefore, 6)} → ${hre.ethers.formatUnits(balanceAfter, 6)} USDC\n`);
    } catch (error) {
      console.log(`  ❌ 失败: ${error.message}\n`);
    }
  }

  // 显示最终余额
  console.log("===== 最终余额 =====\n");
  for (const user of testUsers) {
    const balance = await usdc.balanceOf(user.address);
    console.log(`${user.name.padEnd(20)} ${user.address} 余额: ${hre.ethers.formatUnits(balance, 6)} USDC`);
  }

  console.log("\n✅ 批量铸造完成!\n");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
