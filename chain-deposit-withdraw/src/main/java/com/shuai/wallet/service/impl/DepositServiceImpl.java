package com.shuai.wallet.service.impl;


import com.shuai.wallet.config.ETHConfig;
import com.shuai.wallet.enums.CurrencyCodeEnum;
import com.shuai.wallet.enums.TransactionStatusEnum;
import com.shuai.wallet.model.PendingTransaction;
import com.shuai.wallet.service.DepositService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DepositServiceImpl implements DepositService {

    @Resource
    private ETHConfig ethConfig;

    // Key: Transaction Hash (String)
    // Value: PendingTransaction object
    private final Map<String, PendingTransaction> transactionStore = new ConcurrentHashMap<>();


    // key 是用户充值地址，value是uid
    Map<String, Integer> userDepositAddressMap = new ConcurrentHashMap<>();

    private static final int REQUIRED_CONFIRMATIONS = 6;

    /**
     * Initializes the service with some mock user addresses for demonstration.
     */
    @PostConstruct
    public void init() {
        userDepositAddressMap.put("0x6e575dbC4C359bcB6b5cb59756b64De8aB2BA66c".toLowerCase(), 1);
    }

    /**
     * Called by the listener for each transaction in a new block.
     * It checks if the transaction is a deposit to one of our users.
     */
    public void processDeposit(Transaction tx) {
        String toAddress = tx.getTo().toLowerCase();

        // 1. Check if the destination address is one of our users' addresses.
        // 检查是否是 ETH 充值
        if (userDepositAddressMap.containsKey(toAddress.toLowerCase())) {
            processETHDeposit(tx);
            return;
        }
        // USDT 充值通过事件监听处理，这里不再处理
//        if (isUSDTTranfer(tx)) {
//            processUSDTDeposit(tx);
//            return;
//        }

    }

    /**
     * Called by the listener for every new block.
     * Updates the confirmation count for all pending transactions.
     */
    public void updatePendingConfirmations(long latestBlockNumber) {
        for (PendingTransaction ptx : transactionStore.values()) {
            if (ptx.getStatus() == TransactionStatusEnum.CONFIRMED) {
                continue;
            }

            long confirmations = latestBlockNumber - ptx.getBlockNumber() + 1;

            if (confirmations >= REQUIRED_CONFIRMATIONS) {
                ptx.setConfirmations(confirmations);
                ptx.setStatus(TransactionStatusEnum.CONFIRMED);

                // 根据币种显示不同的单位
                String valueStr;
                if ("ETH".equals(ptx.getCurrency())) {
                    BigDecimal valueInEther = Convert.fromWei(ptx.getValue().toString(), Convert.Unit.ETHER);
                    valueStr = valueInEther + " ETH";
                } else if ("USDT".equals(ptx.getCurrency())) {
                    BigDecimal valueInUSDT = new BigDecimal(ptx.getValue()).divide(BigDecimal.valueOf(1000000));
                    valueStr = valueInUSDT + " USDT";
                } else {
                    valueStr = ptx.getValue().toString();
                }

                log.info("{} Deposit Confirmed! Hash: {}, To: {}, Value: {}, Confirmations: {}",
                        ptx.getCurrency(), ptx.getTxHash(), ptx.getToAddress(), valueStr, confirmations);

                // 获取uid并给用户充值
                Integer uid = userDepositAddressMap.get(ptx.getToAddress().toLowerCase());
                // 根据币种给用户充值对应金额 (略)

                transactionStore.remove(ptx.getTxHash());
            } else {
                ptx.setConfirmations(confirmations);
                log.info("{} Deposit Pending. Hash: {}, Confirmations: {}/{}",
                        ptx.getCurrency(), ptx.getTxHash(), confirmations, REQUIRED_CONFIRMATIONS);
            }
        }
    }

    @Override
    public void processUSDTDepositFromEvent(String txHash, String fromAddress, String toAddress, BigInteger value, long blockNumber) {
        if (!userDepositAddressMap.containsKey(toAddress.toLowerCase())) {
            return;
        }
        // 检查是否已经处理过
        if (transactionStore.containsKey(txHash)) {
            return;
        }

        // 创建待确认交易
        PendingTransaction pendingTx = new PendingTransaction(
                txHash,
                fromAddress,
                toAddress,
                value,
                blockNumber,
                CurrencyCodeEnum.USDT.getCode()
        );

        transactionStore.put(txHash, pendingTx);

        // USDT 有 6 位小数
        BigDecimal valueInUSDT = new BigDecimal(value).divide(BigDecimal.valueOf(1000000));
        log.info("New USDT Deposit Found! Hash: {}, From: {}, To: {}, Value: {} USDT, Block: {}",
                txHash, fromAddress, toAddress, valueInUSDT, blockNumber);
    }

    private void processETHDeposit(Transaction tx) {
        String toAddress = tx.getTo().toLowerCase();

        // 检查是否已经处理过
        if (transactionStore.containsKey(tx.getHash())) {
            return;
        }

        // 创建待确认交易
        PendingTransaction pendingTx = new PendingTransaction(
                tx.getHash(),
                tx.getFrom(),
                toAddress,
                tx.getValue(),
                tx.getBlockNumber().longValue(),
                CurrencyCodeEnum.ETH.getCode() // 添加币种标识
        );

        transactionStore.put(tx.getHash(), pendingTx);
        BigDecimal valueInEther = Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER);
        log.info("New ETH Deposit Found! Hash: {}, To: {}, Value: {} ETH, Block: {}",
                tx.getHash(), toAddress, valueInEther, tx.getBlockNumber());
    }

    // 监听 ERC20 的 Transfer 事件来处理 USDT 充值
    @Deprecated
    private void processUSDTDeposit(Transaction tx) {
        // 解析 USDT transfer 的 input data
        String input = tx.getInput();
        if (input == null || input.length() < 10) {
            return;
        }

        // transfer 方法的签名: transfer(address,uint256)
        // 方法ID: 0xa9059cbb
        if (!input.startsWith("0xa9059cbb")) {
            return;
        }

        try {
            // 解析参数：to address 和 amount
            String toAddressHex = "0x" + input.substring(10, 74); // 32字节的地址
            String amountHex = "0x" + input.substring(74, 138);   // 32字节的数量

            // 转换为标准地址格式
            String toAddress = "0x" + toAddressHex.substring(26); // 去掉前导零
            BigInteger amount = new BigInteger(amountHex.substring(2), 16);

            // 检查是否是我们的用户地址
            if (!userDepositAddressMap.containsKey(toAddress.toLowerCase())) {
                return;
            }

            // 检查是否已经处理过
            if (transactionStore.containsKey(tx.getHash())) {
                return;
            }

            // 创建待确认交易
            PendingTransaction pendingTx = new PendingTransaction(
                    tx.getHash(),
                    tx.getFrom(),
                    toAddress,
                    amount,
                    tx.getBlockNumber().longValue(),
                    CurrencyCodeEnum.USDT.getCode() // 添加币种标识
            );

            transactionStore.put(tx.getHash(), pendingTx);

            // USDT 有 6 位小数
            BigDecimal valueInUSDT = new BigDecimal(amount).divide(BigDecimal.valueOf(1000000));
            log.info("New USDT Deposit Found! Hash: {}, To: {}, Value: {} USDT, Block: {}",
                    tx.getHash(), toAddress, valueInUSDT, tx.getBlockNumber());

        } catch (Exception e) {
            log.error("Failed to parse USDT transfer: {}", tx.getHash(), e);
        }
    }


    private boolean isUSDTTranfer(Transaction tx) {
        return ethConfig.getUsdtContract().equalsIgnoreCase(tx.getTo());
    }

}