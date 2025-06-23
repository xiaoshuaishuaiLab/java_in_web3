package com.shuai.wallet.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuai.wallet.service.DepositService;
import com.shuai.wallet.service.WithdrawService;
import com.shuai.wallet.service.impl.WithdrawServiceImpl;
import com.shuai.wallet.util.ETHUtil;
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


    @Qualifier("wsWeb3j")
    @Resource
    private Web3j wsWeb3j;

    @Resource
    private WithdrawService withdrawService;

    @Resource
    private DepositService depositService;

//    @PostConstruct
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
        ETHUtil.printBlockInfoAsJsonLine(block);

        long blockNumber = block.getNumber().longValue();
        log.info("正在处理新区块: #{}", blockNumber);

        for (EthBlock.TransactionResult<Transaction> txResult : block.getTransactions()) {
            Transaction tx = txResult.get();

            if (tx.getTo() == null || tx.getValue().equals(BigInteger.ZERO)) {
                continue;
            }

            withdrawService.processWithdraw(tx);
            depositService.processDeposit(tx);
        }

        withdrawService.updatePendingConfirmations(blockNumber);
        depositService.updatePendingConfirmations(blockNumber);
    }

    private void handleError(Throwable error) {
        log.error("区块链监听发生错误: {}", error.getMessage(), error);
    }
}
