const hre = require("hardhat");

async function main() {
  const usdcAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
  
  console.log("测试 USDC 合约:", usdcAddress);
  
  // 获取合约
  const usdc = await hre.ethers.getContractAt("MockUSDC", usdcAddress);
  const [signer] = await hre.ethers.getSigners();
  
  console.log("当前账户:", signer.address);
  
  // 测试 basic function
  try {
    const name = await usdc.name();
    console.log("Token name:", name);
  } catch (e) {
    console.log("❌ 调用 name() 失败:", e.message);
    return;
  }
  
  // 查询余额
  const balance = await usdc.balanceOf(signer.address);
  console.log("USDC 余额:", hre.ethers.formatUnits(balance, 6));
  
  console.log("✅ USDC 合约正常工作!");
}

main()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error(error);
    process.exit(1);
  });
