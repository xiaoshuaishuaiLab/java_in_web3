package com.shuai.wallet.service.impl;


import com.shuai.wallet.enums.TransactionStatusEnum;
import com.shuai.wallet.model.PendingTransaction;
import com.shuai.wallet.service.DepositService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DepositServiceImpl implements DepositService {

    // Key: Transaction Hash (String)
    // Value: PendingTransaction object
    private final Map<String, PendingTransaction> transactionStore = new ConcurrentHashMap<>();


    // key 是用户充值地址，value是uid
    Map<String ,Integer> userDepositAddressMap = new ConcurrentHashMap<>();

    private static final int REQUIRED_CONFIRMATIONS = 6;

    /**
     * Initializes the service with some mock user addresses for demonstration.
     */
    @PostConstruct
    public void init() {
        userDepositAddressMap.put("0x6e575dbC4C359bcB6b5cb59756b64De8aB2BA66c",1);
    }

    /**
     * Called by the listener for each transaction in a new block.
     * It checks if the transaction is a deposit to one of our users.
     */
    public void processDeposit(Transaction tx) {
        String toAddress = tx.getTo().toLowerCase();

        // 1. Check if the destination address is one of our users' addresses.
        if (!userDepositAddressMap.containsKey(toAddress)) {
            return; // Not a deposit for our users, ignore.
        }

        // 2. Check if we are already tracking this transaction.
        if (transactionStore.containsKey(tx.getHash())) {
            return; // Already processed, ignore.
        }

        // 3. This is a new, relevant deposit. Record it as PENDING.
        PendingTransaction pendingTx = new PendingTransaction(
                tx.getHash(),
                tx.getFrom(),
                toAddress,
                tx.getValue(),
                tx.getBlockNumber().longValue()
        );

        transactionStore.put(tx.getHash(), pendingTx);
        BigDecimal valueInEther = Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER);
        log.info("New Deposit Found! Hash: {}, To: {}, Value: {} ETH, Block: {}",
                tx.getHash(), toAddress, valueInEther, tx.getBlockNumber());
    }

    /**
     * Called by the listener for every new block.
     * Updates the confirmation count for all pending transactions.
     */
    public void updatePendingConfirmations(long latestBlockNumber) {
        for (PendingTransaction ptx : transactionStore.values()) {
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

                // 获取uid
                Integer uid = userDepositAddressMap.get(ptx.getToAddress());
                // 给用户充值上对应的金额 (略)

                transactionStore.remove(ptx.getTxHash());

            } else {
                // --- STILL PENDING ---
                ptx.setConfirmations(confirmations);
                log.info("Deposit Pending. Hash: {}, Confirmations: {}/{}",
                        ptx.getTxHash(), confirmations, REQUIRED_CONFIRMATIONS);
            }
        }
    }
}