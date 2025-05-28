package com.shuai.bitcoin.mastering;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.*;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.DumpedPrivateKey;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TransactionExample {

    //  bitcoinJ的如果没有download全量的交易数据，无法根据交易hash获取交易详情,可以通过如下api获取信息
    // todo   https://www.blockchain.com/explorer/api/blockchain_api
    public void getTransactionById(String txId) throws Exception {
        Sha256Hash txHash = Sha256Hash.wrap(txId);

    }

    // 发送一笔交易
    public void sendTransaction() throws Exception {

    }
//    public static void main(String[] args) throws Exception {
//        // 使用测试网参数
//        NetworkParameters params = TestNet3Params.get();
//
//        // 创建内存区块存储
//        BlockStore blockStore = new MemoryBlockStore(params);
//
//        // 创建 PeerGroup 用于网络连接
//        PeerGroup peerGroup = new PeerGroup(params, blockStore);
//        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
//
//        // 假设你已经有了 DeterministicKey 对象
//        DeterministicKey sourceKey = ...; // 你的 DeterministicKey 对象
//
//        // 将 DeterministicKey 转换为 ECKey
//        ECKey ecKey = ECKey.fromPrivate(sourceKey.getPrivKey());
//
//        // 目标地址
//        Address destinationAddress = LegacyAddress.fromBase58(params, "mn1B9zCgtPY9TvGQUR6L1L9rhiAvtTwFZ3");
//
//        // 找零地址
//        Address changeAddress = LegacyAddress.fromBase58(params, "n1WyhSmnSYc94YHkCvWuWtyYLX9khmt9ye");
//
//        // 转账金额 (0.1 mBTC = 0.0001 BTC)
//        Coin amount = Coin.valueOf(10000); // 10000 satoshis = 0.1 mBTC
//
//        // 获取UTXO（你需要提供实际的UTXO信息）
//        Sha256Hash txId = Sha256Hash.wrap("你的UTXO交易ID");
//        int vout = 0; // UTXO的输出索引
//        Coin utxoAmount = Coin.valueOf(20000); // UTXO的金额，需要大于转账金额+手续费
//
//        // 创建交易
//        Transaction transaction = new Transaction(params);
//
//        // 添加输入
//        TransactionOutPoint outPoint = new TransactionOutPoint(params, vout, txId);
//        TransactionInput input = new TransactionInput(params, transaction, new byte[0], outPoint, utxoAmount);
//        transaction.addInput(input);
//
//        // 添加输出
//        transaction.addOutput(amount, destinationAddress);
//
//        // 计算找零金额 (减去手续费)
//        Coin fee = Coin.valueOf(1000); // 假设手续费为 1000 satoshis
//        Coin changeAmount = utxoAmount.subtract(amount).subtract(fee);
//
//        if (changeAmount.isGreaterThan(Coin.ZERO)) {
//            transaction.addOutput(changeAmount, changeAddress);
//        }
//
//        // 签名交易
//        Script scriptPubKey = ScriptBuilder.createP2PKHOutputScript(ecKey);
//        TransactionSignature signature = transaction.calculateSignature(
//                0, // 输入索引
//                ecKey,
//                scriptPubKey,
//                Transaction.SigHash.ALL,
//                false
//        );
//
//        // 创建输入脚本
//        Script inputScript = ScriptBuilder.createInputScript(signature, ecKey);
//        input.setScriptSig(inputScript);
//
//        // 验证交易
//        transaction.verify();
//
//        // 广播交易
//        peerGroup.start();
//        peerGroup.broadcastTransaction(transaction).broadcast();
//
//        System.out.println("Transaction broadcasted. TXID: " + transaction.getTxId());
//
//        // 关闭连接
//        peerGroup.stop();
//    }

}
