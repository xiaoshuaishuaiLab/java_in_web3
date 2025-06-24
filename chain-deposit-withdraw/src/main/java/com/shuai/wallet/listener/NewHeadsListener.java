package com.shuai.wallet.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shuai.wallet.config.ETHConfig;
import com.shuai.wallet.service.DepositService;
import com.shuai.wallet.service.WithdrawService;
import com.shuai.wallet.service.impl.WithdrawServiceImpl;
import com.shuai.wallet.util.ETHUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NewHeadsListener {

    static ObjectMapper mapper = new ObjectMapper();

    @Qualifier("wsWeb3j")
    @Resource
    private Web3j wsWeb3j;

    @Qualifier("httpWeb3j")
    @Resource
    private Web3j web3j;



    @Resource
    private WithdrawService withdrawService;

    @Resource
    private DepositService depositService;

    @Resource
    private ETHConfig ethConfig;

    // Transfer 事件的主题 (keccak256("Transfer(address,address,uint256)"))
    private static final String TRANSFER_EVENT_TOPIC = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    //    @PostConstruct
    public void start() {
        log.info("启动区块链监听器...");

        wsWeb3j.blockFlowable(true).subscribe(
                this::processBlock,
                this::handleError
        );
    }

    @SneakyThrows
    public void processBlock(EthBlock ethBlock) {
        EthBlock.Block block = ethBlock.getBlock();

        if (block == null) {
            log.warn("收到了一个空的区块通知。");
            return;
        }
        Map<String, Object> blockInfoAsMap = ETHUtil.getBlockInfoAsMap(block);

        log.info("blockInfo = {}", mapper.writeValueAsString(blockInfoAsMap));

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

        // 获取区块的日志（事件）
        processBlockLogs(blockNumber);


        withdrawService.updatePendingConfirmations(blockNumber);
        depositService.updatePendingConfirmations(blockNumber);
    }

    private void processBlockLogs(long blockNumber) {
        try {
            // 获取区块中的所有日志, 此处用
            EthLog ethLog = web3j.ethGetLogs(
                    new EthFilter(
                            DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                            DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)),
                            ethConfig.getUsdtContract()
                    )
            ).send();

            if (ethLog.hasError()) {
                log.error("获取区块日志失败: {}", ethLog.getError().getMessage());
                return;
            }

            // 处理每个日志

            for (EthLog.LogResult logResult :ethLog.getLogs() ) {
                processTransferEvent((Log) logResult.get());
            }


        } catch (Exception e) {
            log.error("处理区块日志失败: {}", e.getMessage(), e);
        }
    }

    private void processTransferEvent(Log transferLog) {
        // 检查是否是 Transfer 事件
        if (transferLog.getTopics().size() < 3 ||
                !TRANSFER_EVENT_TOPIC.equals(transferLog.getTopics().get(0))) {
            return;
        }

        try {
            // 解析 Transfer 事件的参数
            String fromAddress = "0x" + transferLog.getTopics().get(1).substring(26); // 去掉前导零
            String toAddress = "0x" + transferLog.getTopics().get(2).substring(26);   // 去掉前导零
            String valueHex = transferLog.getData();
            BigInteger value = new BigInteger(valueHex.substring(2), 16); // 去掉0x前缀


            // 处理 USDT 充值
            depositService.processUSDTDepositFromEvent(
                    transferLog.getTransactionHash(),
                    fromAddress,
                    toAddress,
                    value,
                    transferLog.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("解析 Transfer 事件失败: {}", transferLog.getTransactionHash(), e);
        }
    }

    private void handleError(Throwable error) {
        log.error("区块链监听发生错误: {}", error.getMessage(), error);
    }
}
