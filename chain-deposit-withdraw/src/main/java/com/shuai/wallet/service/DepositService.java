package com.shuai.wallet.service;

import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;

public interface DepositService {
    void processDeposit(Transaction tx);

    void updatePendingConfirmations(long latestBlockNumber);

    void processUSDTDepositFromEvent(String txHash, String fromAddress, String toAddress, BigInteger value, long blockNumber);
}
