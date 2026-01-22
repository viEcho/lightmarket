// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "./OrderbookMarket.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";

contract MarketFactory {
    IERC20 public immutable usdc;

    event MarketCreated(
        address market,
        address creator,
        uint256 endTime,
        uint256 initialLiquidity
    );

    constructor(address _usdc) {
        usdc = IERC20(_usdc);
    }

    function createMarket(
        bytes32 marketId,        // 链下 market_id 的 hash
        uint256 endTime,
        uint256 initialLiquidity
    ) external returns (address) {
        require(endTime > block.timestamp, "invalid end time");
        require(initialLiquidity > 0, "zero liquidity");

        // 1️⃣ 先把钱拉进 Factory
        usdc.transferFrom(
            msg.sender,
            address(this),
            initialLiquidity
        );

        // 2️⃣ 部署 Market
        OrderbookMarket market = new OrderbookMarket(
            address(usdc),
            msg.sender,
            endTime,
            initialLiquidity
        );

        // 3️⃣ 把钱转给 Market
        usdc.transfer(address(market), initialLiquidity);

        emit MarketCreated(
            address(market),
            msg.sender,
            endTime,
            initialLiquidity
        );

        return address(market);
    }
}
