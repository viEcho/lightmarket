package com.market.business.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * PredictionMarket 合约包装类
 * 用于查询 YES/NO 价格
 */
public class PredictionMarket {

    private final Web3j web3j;
    private final String contractAddress;

    public PredictionMarket(String contractAddress, Web3j web3j) {
        this.web3j = web3j;
        this.contractAddress = contractAddress;
    }

    /**
     * 加载合约实例
     */
    public static PredictionMarket load(String contractAddress, Web3j web3j) {
        return new PredictionMarket(contractAddress, web3j);
    }

    /**
     * 获取 YES 价格 (返回 0-100 的整数)
     */
    public BigInteger getYesPrice() throws Exception {
        // 构建函数调用: getYesPrice() -> uint256
        Function function = new Function(
            "getYesPrice",
            Arrays.asList(),
            Collections.singletonList(new TypeReference<Uint256>() {}));

        // 编码函数调用
        String encodedFunction = encodeFunction(function);

        // 创建交易对象
        Transaction transaction = Transaction.createEthCallTransaction(
            null,  // from address (只读调用可以为null)
            contractAddress,
            encodedFunction);

        // 调用合约
        EthCall response = web3j.ethCall(
                transaction,
                DefaultBlockParameterName.LATEST
        ).sendAsync().get();

        // 解析返回值
        String value = response.getValue();
        if (value == null || value.equals("0x") || value.length() < 3) {
            return BigInteger.ZERO;
        }

        // 去掉0x前缀并转换为BigInteger
        return new BigInteger(value.substring(2), 16);
    }

    /**
     * 获取 NO 价格 (返回 0-100 的整数)
     */
    public BigInteger getNoPrice() throws Exception {
        // 构建函数调用: getNoPrice() -> uint256
        Function function = new Function(
            "getNoPrice",
            Arrays.asList(),
            Collections.singletonList(new TypeReference<Uint256>() {}));

        // 编码函数调用
        String encodedFunction = encodeFunction(function);

        // 创建交易对象
        Transaction transaction = Transaction.createEthCallTransaction(
            null,  // from address (只读调用可以为null)
            contractAddress,
            encodedFunction);

        // 调用合约
        EthCall response = web3j.ethCall(
                transaction,
                DefaultBlockParameterName.LATEST
        ).sendAsync().get();

        // 解析返回值
        String value = response.getValue();
        if (value == null || value.equals("0x") || value.length() < 3) {
            return BigInteger.ZERO;
        }

        // 去掉0x前缀并转换为BigInteger
        return new BigInteger(value.substring(2), 16);
    }

    /**
     * 编码函数调用
     */
    private String encodeFunction(Function function) {
        // 使用FunctionEncoder编码
        return org.web3j.abi.DefaultFunctionEncoder.encode(function);
    }
}
