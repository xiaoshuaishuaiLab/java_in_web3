package com.shuai.wallet.service;

import org.web3j.protocol.core.methods.response.Transaction;

public interface DepositService {
    void processDeposit(Transaction tx);
    void updatePendingConfirmations(long latestBlockNumber);
}
