// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

import "./OrderbookMarket.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

contract MarketFactory {
    IERC20 public immutable usdc;

    // marketId -> market address 映射
    mapping(bytes32 => address) public markets;

    // 所有市场的marketId列表
    bytes32[] public allMarketIds;

    // 市场数量
    uint256 public marketCount;

    event MarketCreated(
        bytes32 indexed marketId,
        address indexed market,
        address indexed creator,
        uint256 endTime,
        uint256 initialLiquidity
    );

    constructor(address _usdc) {
        usdc = IERC20(_usdc);
    }

    function createMarket(
        bytes32 marketId,
        uint256 endTime,
        uint256 initialLiquidity
    ) external returns (address) {
        require(endTime > block.timestamp, "invalid end time");
        require(initialLiquidity > 0, "zero liquidity");
        require(markets[marketId] == address(0), "market already exists");

        // 1️⃣ 先把钱拉进 Factory
        require(
            usdc.transferFrom(msg.sender, address(this), initialLiquidity),
            "usdc transfer failed"
        );

        // 2️⃣ 部署 Market
        PredictionMarket market = new PredictionMarket(
            address(usdc),
            msg.sender,
            endTime,
            initialLiquidity,
            marketId
        );

        // 3️⃣ 把钱转给 Market
        require(
            usdc.transfer(address(market), initialLiquidity),
            "usdc transfer to market failed"
        );

        // 4️⃣ 记录marketId映射
        markets[marketId] = address(market);
        allMarketIds.push(marketId);
        marketCount++;

        emit MarketCreated(
            marketId,
            address(market),
            msg.sender,
            endTime,
            initialLiquidity
        );

        return address(market);
    }

    // 查询市场地址
    function getMarketAddress(bytes32 marketId) external view returns (address) {
        address market = markets[marketId];
        require(market != address(0), "market not found");
        return market;
    }

    // 获取所有市场ID
    function getAllMarketIds() external view returns (bytes32[] memory) {
        return allMarketIds;
    }

    // 分页获取市场ID
    function getMarketIds(uint256 offset, uint256 limit) external view returns (bytes32[] memory) {
        require(offset < allMarketIds.length, "offset out of bounds");

        uint256 end = offset + limit;
        if (end > allMarketIds.length) {
            end = allMarketIds.length;
        }

        uint256 length = end - offset;
        bytes32[] memory pageIds = new bytes32[](length);

        for (uint256 i = 0; i < length; i++) {
            pageIds[i] = allMarketIds[offset + i];
        }

        return pageIds;
    }

    // 检查市场是否存在
    function marketExists(bytes32 marketId) external view returns (bool) {
        return markets[marketId] != address(0);
    }
}
