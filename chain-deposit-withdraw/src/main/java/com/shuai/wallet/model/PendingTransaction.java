package com.shuai.wallet.model;

import com.shuai.wallet.enums.TransactionStatusEnum;
import lombok.Data;
import java.math.BigInteger;

@Data
public class PendingTransaction {

    private final String txHash;
    private final String fromAddress;
    private final String toAddress;
    private final BigInteger value;
    private final long blockNumber;
    private final String currency; // 新增币种字段

    private long confirmations;
    private TransactionStatusEnum status;

    public PendingTransaction(String txHash, String fromAddress, String toAddress, BigInteger value, long blockNumber, String currency) {
        this.txHash = txHash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.value = value;
        this.blockNumber = blockNumber;
        this.currency = currency;
        this.confirmations = 1; // Start with 1 confirmation
        this.status = TransactionStatusEnum.PENDING;
    }
} 