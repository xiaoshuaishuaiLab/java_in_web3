package com.shuai.wallet.service;

import org.web3j.protocol.core.methods.response.Transaction;

public interface WithdrawService {
    void processWithdraw(Transaction tx);

    void updatePendingConfirmations(long latestBlockNumber);
}
