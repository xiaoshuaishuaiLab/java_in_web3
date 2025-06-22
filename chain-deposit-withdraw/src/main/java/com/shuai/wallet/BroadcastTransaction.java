package com.shuai.wallet;

import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

public class BroadcastTransaction {

    public static void main(String[] args) throws Exception {
        // 1. 确保使用Sepolia的URL
        String sepoliaInfuraUrl = "https://sepolia.infura.io/v3/8c74af6f0b934262ac0c69351593a9ee";
        Web3j web3j = Web3j.build(new HttpService(sepoliaInfuraUrl));

        // 你已经生成的、正确的裸交易哈希
        String signedTxHex = "0x02f87283aa36a780831158d68322b1ac8252089449dcde8b9e56dd9bd172002db5233c350cc7abf987038d7ea4c6800080c080a05fa6a4bb3cb8ac3da487503cf9ac7d9c306aec2703e4eaa885b98b798783ec47a024537c37b52b5a658848767eaa678913d9a273a86e75a4f3a42bbb552d80acb8";

        System.out.println("Attempting to broadcast transaction to Sepolia...");

        // 2. 发送交易到Sepolia节点
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(signedTxHex).send();

        // 3. 处理结果
        if (ethSendTransaction.hasError()) {
            // 如果仍然有错误，打印出来
            System.err.println("Transaction failed!");
            System.err.println("Error: " + ethSendTransaction.getError().getMessage());
        } else {
            // 成功广播
            String txHash = ethSendTransaction.getTransactionHash();
            System.out.println("Transaction broadcast successfully!");
            System.out.println("Transaction Hash: " + txHash);
            System.out.println("View on Sepolia Etherscan: https://sepolia.etherscan.io/tx/" + txHash);
        }
    }
}
