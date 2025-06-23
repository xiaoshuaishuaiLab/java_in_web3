package com.shuai.wallet.service;

import com.shuai.wallet.bo.PendingTransactionBO;
import com.shuai.wallet.enums.TransactionStatusEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WithdrawService {

    private final Map<String, PendingTransactionBO> transactionStore = new ConcurrentHashMap<>();

    private final Set<String> platformAddress = new HashSet<>();

    private static final int REQUIRED_CONFIRMATIONS = 6;

    /**
     * Initializes the service with some mock user addresses for demonstration.
     */
    @PostConstruct
    public void init() {
        // Add a few test addresses. Remember to use lowercase for consistent matching.
        platformAddress.add("0x6e575dbC4C359bcB6b5cb59756b64De8aB2BA66c".toLowerCase());
    }

    /**
     * Called by the listener for each transaction in a new block.
     * It checks if the transaction is a deposit to one of our users.
     */
    public void processWithdraw(Transaction tx) {
        String fromAddress = tx.getFrom().toLowerCase();

        // 1. Check if the destination address is one of our users' addresses.
        if (!platformAddress.contains(fromAddress)) {
            return; // Not a deposit for our users, ignore.
        }

        // 2. Check if we are already tracking this transaction.
        if (transactionStore.containsKey(tx.getHash())) {
            return; // Already processed, ignore.
        }

        // 3. This is a new, relevant deposit. Record it as PENDING.
        PendingTransactionBO pendingTx = new PendingTransactionBO(
                tx.getHash(),
                tx.getFrom(),
                fromAddress,
                tx.getValue(),
                tx.getBlockNumber().longValue()
        );

        transactionStore.put(tx.getHash(), pendingTx);
        BigDecimal valueInEther = Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER);
        log.info("New Deposit Found! Hash: {}, To: {}, Value: {} ETH, Block: {}",
                tx.getHash(), fromAddress, valueInEther, tx.getBlockNumber());
    }

    /**
     * Called by the listener for every new block.
     * Updates the confirmation count for all pending transactions.
     */
    public void updatePendingConfirmations(long latestBlockNumber) {
        for (PendingTransactionBO ptx : transactionStore.values()) {
            // Skip transactions that are already confirmed.
            if (ptx.getStatus() == TransactionStatusEnum.CONFIRMED) {
                continue;
            }

            // Calculate the number of confirmations.
            long confirmations = latestBlockNumber - ptx.getBlockNumber() + 1;

            if (confirmations >= REQUIRED_CONFIRMATIONS) {
                // --- CONFIRMATION REACHED ---
                ptx.setConfirmations(confirmations);
                ptx.setStatus(TransactionStatusEnum.CONFIRMED);

                BigDecimal valueInEther = Convert.fromWei(ptx.getValue().toString(), Convert.Unit.ETHER);
                log.info("Deposit Confirmed! Hash: {}, To: {}, Value: {} ETH, Confirmations: {}",
                        ptx.getTxHash(), ptx.getToAddress(), valueInEther, confirmations);
                // 更新用户余额 略
                // remove pending cache
                transactionStore.remove(ptx.getTxHash());

            } else {
                ptx.setConfirmations(confirmations);
                log.info("Deposit Pending. Hash: {}, Confirmations: {}/{}",
                        ptx.getTxHash(), confirmations, REQUIRED_CONFIRMATIONS);
            }
        }
    }
} 