// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
contract OrderbookMarket {
    IERC20 public immutable usdc;
    address public immutable creator;

    uint256 public immutable endTime;
    uint256 public immutable unlockTime;

    bool public finalized;

    mapping(address => uint256) public locked;
    mapping(address => uint256) public balance;

    constructor(
        address _usdc,
        address _creator,
        uint256 _endTime,
        uint256 _initialLiquidity
    ) {
        usdc = IERC20(_usdc);
        creator = _creator;
        endTime = _endTime;

        // 🔒 结束时间 + 24 小时
        unlockTime = _endTime + 24 hours;

        // 🔒 初始流动性直接锁死
        locked[_creator] = _initialLiquidity;
    }

    modifier onlyAfterUnlock() {
        require(
            block.timestamp >= unlockTime,
            "liquidity still locked"
        );
        _;
    }

    function finalize() external {
        require(
            block.timestamp >= endTime,
            "market not ended"
        );
        finalized = true;
    }

    function claim() external onlyAfterUnlock {
        require(finalized, "not finalized");

        uint256 amount = locked[msg.sender];
        require(amount > 0, "nothing to claim");

        locked[msg.sender] = 0;
        balance[msg.sender] += amount;
    }

    function withdraw(uint256 amount) external {
        require(balance[msg.sender] >= amount, "insufficient");

        balance[msg.sender] -= amount;
        usdc.transfer(msg.sender, amount);
    }
}
