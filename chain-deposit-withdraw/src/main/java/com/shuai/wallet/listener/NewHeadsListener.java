package com.shuai.wallet.listener;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuai.wallet.service.WithdrawService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigInteger;

@Slf4j
@Service
public class NewHeadsListener {

    private static final ObjectMapper objectMapper = new ObjectMapper();



    @Qualifier("wsWeb3j")
    @Resource
    private Web3j wsWeb3j;

    @Resource
    private WithdrawService withdrawService;

    @PostConstruct
    public void start() {
        log.info("启动区块链监听器...");

        wsWeb3j.blockFlowable(true).subscribe(
                this::processBlock,
                this::handleError
        );
    }

    private void processBlock(EthBlock ethBlock) {

//        log.info("block result = {}", ethBlock.getResult());
        EthBlock.Block block = ethBlock.getBlock();

        if (block == null) {
            log.warn("收到了一个空的区块通知。");
            return;
        }
        try {
            log.info("block = {}",ethBlock.getResult());

        } catch (Exception e) {
            e.printStackTrace();
        }


        long blockNumber = block.getNumber().longValue();
        log.info("正在处理新区块: #{}", blockNumber);

        for (EthBlock.TransactionResult<Transaction> txResult : block.getTransactions()) {
            Transaction tx = txResult.get();

            if (tx.getTo() == null || tx.getValue().equals(BigInteger.ZERO)) {
                continue;
            }

            withdrawService.processWithdraw(tx);
        }

        withdrawService.updatePendingConfirmations(blockNumber);
    }

    private void handleError(Throwable error) {
        log.error("区块链监听发生错误: {}", error.getMessage(), error);
    }
}
