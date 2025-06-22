package com.shuai.wallet.service;

import java.math.BigInteger;

public interface WalletService {

    String getAddress(Integer childNum);

    String signedTransaction(int fromAddressAccountIndex,long chainId, BigInteger nonce, BigInteger gasLimit, String to, BigInteger value, BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas);
}
