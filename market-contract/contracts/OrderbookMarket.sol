// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

/**
 * @title PredictionMarket
 * @notice AMM模式的预测市场合约
 * @dev
 *   核心机制:
 *   1. AMM 流动性池（类似 Uniswap）
 *   2. 用户可以直接买入/卖出（即时成交）
 *   3. 支持链下订单撮合
 *   4. 1 YES + 1 NO = 1 USDC（恒定）
 *   5. 价格动态调整: YES价格 = NO池 / 总池 * 100
 *
 *   价格机制:
 *   - YES价格 = NO池 / (YES池 + NO池) * 100
 *   - NO价格 = YES池 / (YES池 + NO池) * 100
 *   - YES价格上涨时，NO价格下跌
 *   - 买入YES → YES池增加 → YES价格下跌
 *   - 卖出YES → YES池减少 → YES价格上涨
 *
 *   手续费机制:
 *   - AMM直接交易（buyYes/sellYes）: 无手续费
 *   - 链下撮合（executeOrder）: 0.1%手续费（从卖方获得金额中扣除）
 */
contract PredictionMarket {
    IERC20 public immutable usdc;
    address public immutable creator;

    uint256 public immutable endTime;
    uint256 public immutable unlockTime;
    bytes32 public immutable marketId;

    bool public finalized;

    // ========== AMM 流动性池 ==========
    // 池子包含 YES 和 NO 代币
    uint256 public yesPool;
    uint256 public noPool;

    // 用户代币余额
    mapping(address => uint256) public yesBalance;
    mapping(address => uint256) public noBalance;

    // 初始流动性锁仓（市场创建者注入的流动性）
    mapping(address => uint256) public lockedLiquidity;
    mapping(address => uint256) public withdrawableBalance;

    // 手续费累积（YES池和NO池分别累积）
    uint256 public yesPoolFees;   // YES池累积的手续费
    uint256 public noPoolFees;    // NO池累积的手续费

    // 后期流动性提供者（可选功能，当前版本禁用）
    // bool public liquidityEnabled;
    // uint256 public totalLiquidityShares;
    // mapping(address => uint256) public liquidityShares;

    // 手续费率 (基点, 100 = 1%)
    uint256 public constant FEE_BPS = 10; // 0.1%
    uint256 public constant FEE_DENOMINATOR = 10000;

    // 事件
    event Initialized(uint256 yesAmount, uint256 noAmount, uint256 initialPrice);
    event BoughtYes(address indexed user, uint256 usdcSpent, uint256 yesReceived, uint256 newPrice);
    event SoldYes(address indexed user, uint256 yesSold, uint256 usdcReceived, uint256 newPrice);
    event OrderMatched(
        address indexed buyer,
        address indexed seller,
        uint256 yesAmount,
        uint256 price,  // 价格 (cents)
        uint256 usdcAmount
    );
    event MarketFinalized(bool yesWon);
    event LiquidityClaimed(address indexed user, uint256 amount);
    event WinningsClaimed(address indexed user, uint256 amount);

    constructor(
        address _usdc,
        address _creator,
        uint256 _endTime,
        uint256 _initialLiquidity,
        bytes32 _marketId
    ) {
        usdc = IERC20(_usdc);
        creator = _creator;
        endTime = _endTime;
        marketId = _marketId;

        // 解锁时间 = 结束时间 + 24 小时
        unlockTime = _endTime + 24 hours;

        // 初始流动性: 1 USDC = 1 YES + 1 NO
        // 例如: 1000 USDC → 500 YES + 500 NO
        // ⚠️ 初始流动性永久锁定，不可追加或提取
        uint256 yesAmount = _initialLiquidity / 2;
        uint256 noAmount = _initialLiquidity / 2;

        yesPool = yesAmount;
        noPool = noAmount;

        // 锁定初始流动性
        lockedLiquidity[_creator] = _initialLiquidity;

        uint256 initialPrice = getYesPrice();

        emit Initialized(yesAmount, noAmount, initialPrice);
    }

    // ========== Modifiers ==========
    modifier onlyBeforeEnd() {
        require(block.timestamp < endTime, "market ended");
        _;
    }

    modifier onlyAfterEnd() {
        require(block.timestamp >= endTime, "market not ended");
        _;
    }

    modifier onlyAfterUnlock() {
        require(block.timestamp >= unlockTime, "liquidity still locked");
        _;
    }

    // ========== 核心功能：价格查询 ==========

    /**
     * @notice 获取 YES 价格 (0-100 cents)
     * @dev 公式: YES价格 = NO池 / (YES池 + NO池) * 100
     */
    function getYesPrice() public view returns (uint256) {
        uint256 totalPool = yesPool + noPool;
        return (noPool * 100) / totalPool;
    }

    /**
     * @notice 获取 NO 价格 (0-100 cents)
     */
    function getNoPrice() public view returns (uint256) {
        uint256 totalPool = yesPool + noPool;
        return (yesPool * 100) / totalPool;
    }

    // ========== 核心功能：买卖 ==========

    /**
     * @notice 买入 YES 代币（无手续费）
     * @param usdcAmount 愿意支付的最大 USDC 数量
     * @return actualSpent 实际花费的 USDC
     * @return yesReceived 获得的 YES 代币数量
     *
     * @dev 机制:
     * 1. 用户支付 usdcAmount USDC
     * 2. 铸造 usdcAmount YES + usdcAmount NO
     * 3. 将 NO 卖给池子，获得部分 USDC
     * 4. 用户净成本 = usdcAmount - USDC_from_NO
     * 5. YES 池增加 → YES 价格下降
     *
     * 示例:
     * - 初始: YES=500, NO=500, YES价格=50¢
     * - 用户用 100 USDC 买 YES
     * - 获得 100 YES + 100 NO
     * - 卖 100 NO 获得 50 USDC (按50¢计算)
     * - 净成本 = 100 - 50 = 50 USDC
     * - 新池子: YES=600, NO=450
     * - 新价格 = 450/1050 * 100 ≈ 42.86¢
     */
    function buyYes(uint256 usdcAmount) external
        onlyBeforeEnd
        returns (uint256 actualSpent, uint256 yesReceived)
    {
        require(usdcAmount > 0, "zero amount");

        // 1. 转入 USDC
        require(
            usdc.transferFrom(msg.sender, address(this), usdcAmount),
            "transfer failed"
        );

        // 2. 铸造 YES + NO (1 USDC = 1 YES + 1 NO)
        yesReceived = usdcAmount;
        uint256 noAmount = usdcAmount;

        yesBalance[msg.sender] += yesReceived;
        noBalance[msg.sender] += noAmount;

        // 3. 计算卖 NO 能获得多少 USDC（无手续费）
        uint256 currentPrice = getYesPrice();
        uint256 usdcFromNo = (noAmount * currentPrice) / 100;

        // 4. 更新池子
        yesPool += noAmount;
        noPool -= usdcFromNo;

        // 5. 转回 USDC 给用户
        if (usdcFromNo > 0) {
            require(usdc.transfer(msg.sender, usdcFromNo), "transfer failed");
        }

        // 6. 计算净成本
        actualSpent = usdcAmount - usdcFromNo;

        uint256 newPrice = getYesPrice();
        emit BoughtYes(msg.sender, actualSpent, yesReceived, newPrice);
    }

    /**
     * @notice 卖出 YES 代币（无手续费）
     * @param yesAmount 要卖出的 YES 数量
     * @return usdcReceived 获得的 USDC 数量
     *
     * @dev 机制:
     * 1. 用户卖出 YES
     * 2. 需要从池子买 NO (用 USDC)
     * 3. 然后用 YES + NO 赎回 1 USDC
     *
     * 示例:
     * - 当前: YES=600, NO=450, YES价格=42.86¢
     * - 用户卖 100 YES
     * - 买 NO 需要 = 100 * (100-42.86) / 100 = 57.14 USDC
     * - 实际获得 = 100 + 57.14 = 157.14 USDC
     * - 新池子: YES=500, NO=507.14
     * - 新价格 = 50.34%
     */
    function sellYes(uint256 yesAmount) external
        onlyBeforeEnd
        returns (uint256 usdcReceived)
    {
        require(yesAmount > 0, "zero amount");
        require(yesBalance[msg.sender] >= yesAmount, "insufficient balance");

        // 1. 计算能换多少 USDC（无手续费）
        // 卖 YES → 需要从池子买 NO → 然后赎回 1 USDC
        uint256 currentPrice = getYesPrice();
        uint256 usdcToBuyNo = (yesAmount * (100 - currentPrice)) / 100;

        // 2. 更新池子
        // 池子接收 USDC，给用户 NO
        yesPool -= yesAmount;
        noPool += usdcToBuyNo;

        // 3. 用户用 YES + NO 赎回 USDC
        yesBalance[msg.sender] -= yesAmount;
        noBalance[msg.sender] -= yesAmount; // 销毁 NO

        // 4. 转出 USDC (YES + NO = 1 USDC)
        require(usdc.transfer(msg.sender, yesAmount), "transfer failed");

        usdcReceived = yesAmount + usdcToBuyNo;

        uint256 newPrice = getYesPrice();
        emit SoldYes(msg.sender, yesAmount, usdcReceived, newPrice);
    }

    // ========== 链下订单撮合接口 ==========

    /**
     * @notice 执行链下撮合的订单（买卖双方各扣0.1%手续费）
     * @param buyer 买方地址
     * @param seller 卖方地址
     * @param yesAmount YES 代币数量
     * @param price 成交价格 (cents, 0-100)
     *
     * @dev 后端撮合引擎调用此函数:
     * ⚠️ 注意：只有链下撮合才收取手续费，AMM直接交易不收费
     *
     * 手续费机制（双向收费）:
     * - 买方支付: usdcAmount
     * - 买方手续费: usdcAmount * 0.1% → 注入 YES池
     * - 卖方手续费: usdcAmount * 0.1% → 注入 NO池
     * - 卖方实际收到: usdcAmount * (1 - 0.1%)
     * - 总手续费: 0.2% (买卖双方各0.1%)
     *
     * 结算时手续费分配:
     * - 输的那一边的手续费全归创建者
     * - 赢的那一边的手续费也全归创建者
     * - 手续费用于弥补创建者初始投入输掉的那一边
     *
     * 示例:
     * - 链下撮合: 买方用 65¢ 买 100 YES
     * - 买方手续费: 65 * 0.1% = 0.065 USDC → YES池
     * - 卖方手续费: 65 * 0.1% = 0.065 USDC → NO池
     * - 卖方收到: 65 - 0.065 = 64.935 USDC
     * - 总手续费: 0.13 USDC
     */
    function executeOrder(
        address buyer,
        address seller,
        uint256 yesAmount,
        uint256 price
    ) external onlyBeforeEnd {
        require(yesAmount > 0, "zero amount");
        require(price > 0 && price <= 100, "invalid price");
        require(yesBalance[seller] >= yesAmount, "insufficient seller balance");

        // 计算交易金额
        uint256 usdcAmount = (yesAmount * price) / 100;

        // 买卖双方各扣 0.1% 手续费
        uint256 buyerFee = (usdcAmount * FEE_BPS) / FEE_DENOMINATOR;   // 买方手续费
        uint256 sellerFee = (usdcAmount * FEE_BPS) / FEE_DENOMINATOR;  // 卖方手续费
        uint256 sellerReceives = usdcAmount - sellerFee;                // 卖方实际收到

        // 转账 YES
        yesBalance[seller] -= yesAmount;
        yesBalance[buyer] += yesAmount;

        // 转账 USDC
        // 买方支付全额（包含他的手续费）
        require(
            usdc.transferFrom(buyer, address(this), usdcAmount),
            "buyer transfer failed"
        );
        // 卖方收到扣除手续费后的金额
        require(usdc.transfer(seller, sellerReceives), "seller transfer failed");

        // 💰 手续费注入对应池子
        yesPoolFees += buyerFee;   // 买方手续费注入 YES池
        noPoolFees += sellerFee;   // 卖方手续费注入 NO池

        // 向池子注入流动性以平衡价格（不包含手续费）
        uint256 currentPrice = getYesPrice();

        if (price < currentPrice) {
            // 成交价低于当前价，说明池子 YES 太多
            // 吸收部分 YES
            uint256 yesToPool = yesAmount / 10; // 吸收 10%
            yesPool += yesToPool;
            yesBalance[buyer] -= yesToPool;
        }

        emit OrderMatched(buyer, seller, yesAmount, price, usdcAmount);
    }

    // ========== 结算功能 ==========

    /**
     * @notice 结算市场（由 oracle 调用）
     * @param yesWon true=Yes获胜, false=No获胜
     */
    function finalize(bool yesWon) external onlyAfterEnd {
        require(!finalized, "already finalized");
        finalized = yesWon;

        emit MarketFinalized(yesWon);
    }

    /**
     * @notice 领取流动性奖励（包含手续费）
     * @dev 市场结束后 24 小时可提取初始流动性 + 累积手续费
     *
     * 结算时手续费分配:
     * - 创建者初始投入: 1000 USDC → 500 YES + 500 NO
     * - 如果 YES 赢: 创建者获得 YES池(500) + YES池手续费 + NO池手续费
     * - 如果 NO 赢: 创建者获得 NO池(500) + NO池手续费 + YES池手续费
     * - 手续费用于弥补输掉的那一边的损失
     */
    function claimLiquidity() external onlyAfterUnlock {
        require(finalized, "not finalized");

        uint256 initialLiquidity = lockedLiquidity[msg.sender];
        require(initialLiquidity > 0, "nothing to claim");

        // 初始流动性的一半在 YES，一半在 NO
        uint256 initialYes = initialLiquidity / 2;  // 500
        uint256 initialNo = initialLiquidity / 2;   // 500

        // 计算创建者应得的总额
        uint256 totalPayout;

        if (finalized) {
            // YES 赢了
            // 创建者获得: 初始YES(500) + YES池手续费 + NO池手续费
            totalPayout = initialYes + yesPoolFees + noPoolFees;
        } else {
            // NO 赢了
            // 创建者获得: 初始NO(500) + NO池手续费 + YES池手续费
            totalPayout = initialNo + noPoolFees + yesPoolFees;
        }

        // 清除锁定的流动性
        lockedLiquidity[msg.sender] = 0;

        // 清除手续费（已支付给创建者）
        yesPoolFees = 0;
        noPoolFees = 0;

        // 添加到可提取余额
        withdrawableBalance[msg.sender] += totalPayout;

        emit LiquidityClaimed(msg.sender, totalPayout);
    }

    /**
     * @notice 领取获胜代币奖励
     * @param yesWon 获胜方 (true=Yes, false=No)
     */
    function claimWinnings(bool yesWon) external onlyAfterEnd {
        require(finalized, "not finalized");

        uint256 winningAmount;

        if (yesWon) {
            winningAmount = yesBalance[msg.sender];
            require(winningAmount > 0, "no winning tokens");
            yesBalance[msg.sender] = 0;
        } else {
            winningAmount = noBalance[msg.sender];
            require(winningAmount > 0, "no winning tokens");
            noBalance[msg.sender] = 0;
        }

        withdrawableBalance[msg.sender] += winningAmount;

        emit WinningsClaimed(msg.sender, winningAmount);
    }

    /**
     * @notice 提取可提取余额
     */
    function withdraw(uint256 amount) external {
        require(withdrawableBalance[msg.sender] >= amount, "insufficient balance");

        withdrawableBalance[msg.sender] -= amount;
        require(usdc.transfer(msg.sender, amount), "transfer failed");
    }

    // ========== 查询功能 ==========

    /**
     * @notice 查询用户代币余额
     */
    function getUserBalances(address user) external view returns (
        uint256 yesAmount,
        uint256 noAmount,
        uint256 lockedAmount,
        uint256 withdrawable
    ) {
        return (
            yesBalance[user],
            noBalance[user],
            lockedLiquidity[user],
            withdrawableBalance[user]
        );
    }

    /**
     * @notice 查询池子信息
     */
    function getPoolInfo() external view returns (
        uint256 _yesPool,
        uint256 _noPool,
        uint256 _yesPrice,
        uint256 _noPrice
    ) {
        return (
            yesPool,
            noPool,
            getYesPrice(),
            getNoPrice()
        );
    }

    /**
     * @notice 查询买入 YES 的预估成本（无手续费）
     * @param yesAmount 想要购买的 YES 数量
     * @return estimatedCost 预估 USDC 成本
     */
    function getBuyYesEstimate(uint256 yesAmount) external view returns (uint256 estimatedCost) {
        uint256 currentPrice = getYesPrice();
        uint256 usdcFromNo = (yesAmount * currentPrice) / 100;

        // 无手续费
        estimatedCost = yesAmount - usdcFromNo;
    }

    /**
     * @notice 查询卖出 YES 的预估收益（无手续费）
     * @param yesAmount 要卖出的 YES 数量
     * @return estimatedRevenue 预估 USDC 收益
     */
    function getSellYesEstimate(uint256 yesAmount) external view returns (uint256 estimatedRevenue) {
        uint256 currentPrice = getYesPrice();
        uint256 usdcToBuyNo = (yesAmount * (100 - currentPrice)) / 100;

        // 无手续费
        estimatedRevenue = yesAmount + usdcToBuyNo;
    }
}
