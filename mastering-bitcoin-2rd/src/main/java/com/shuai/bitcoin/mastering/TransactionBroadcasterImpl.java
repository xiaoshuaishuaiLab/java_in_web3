package com.shuai.bitcoin.mastering;

import com.shuai.bitcoin.mastering.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.internal.ByteUtils;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionBroadcaster;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class TransactionBroadcasterImpl implements TransactionBroadcaster {

    @Override
    public TransactionBroadcast broadcastTransaction(Transaction tx) {

        CompletableFuture<Transaction> result = new CompletableFuture<>();


        byte[] rawTx  = tx.serialize();
        String s = ByteUtils.formatHex(rawTx);

        String txid = HttpUtil.postRawData("https://mempool.space/testnet/api/tx", s);
        log.info("txid:{}", txid);

//        c62c7afeedf1a32f4d9888c2d2af740ae9794477f78731ea278c4826a22fc51d
        result.complete(tx);
        return TransactionBroadcast.createMockBroadcast(tx, result);
//        return new TransactionBroadcast();
    }


}
