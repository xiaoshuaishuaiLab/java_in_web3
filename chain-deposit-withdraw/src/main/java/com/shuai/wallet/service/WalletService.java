package com.shuai.wallet.service;

import java.math.BigInteger;

public interface WalletService {

    BigInteger getERC20Balance(String contractAddress, String accountAddress);

    String getAddress(Integer childNum);

    String signedTransaction(int fromAddressAccountIndex, long chainId, BigInteger nonce, String to, BigInteger value, BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas);

    // amount USDT最小单位（6位小数）

    String sendUSDT(int fromAddressAccountIndex,long chainId,String usdtContract, String toAddress, BigInteger amount, BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas);
}
