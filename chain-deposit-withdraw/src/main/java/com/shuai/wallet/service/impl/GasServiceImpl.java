package com.shuai.wallet.service.impl;

import com.shuai.wallet.bo.GasParametersBO;
import com.shuai.wallet.service.GasService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;

@Slf4j
@Service
public class GasServiceImpl implements GasService {

    @Resource
    private Web3j web3j;

    // todo https://www.blocknative.com/ 根据这个实现gas逻辑，目前就简单的使用infura
    @Override
    public GasParametersBO getGasParameters() {
        try {
            // 获取最新的区块
            EthBlock.Block latestBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
            // 基础费用
            BigInteger baseFee = latestBlock.getBaseFeePerGas();

            // 优先级费用（小费）- 可以根据网络拥堵情况调整
            BigInteger maxPriorityFeePerGas = BigInteger.valueOf(2_000_000_000L); // 2 Gwei
//            BigInteger maxPriorityFeePerGas = baseFee.multiply(new BigInteger("2")); // 2 Gwei

            // 最大费用 = 基础费用 + 优先级费用 + 缓冲
            BigInteger maxFeePerGas = baseFee.add(maxPriorityFeePerGas).add(BigInteger.valueOf(1_000_000_000L)); // 额外 1 Gwei 缓冲
//            BigInteger maxFeePerGas = baseFee.add(maxPriorityFeePerGas).add(baseFee); // 额外 1 Gwei 缓冲
            return new GasParametersBO(baseFee, maxPriorityFeePerGas, maxFeePerGas);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
