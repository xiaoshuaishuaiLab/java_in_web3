package com.shuai.bitcoin.mastering;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.springframework.stereotype.Component;

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


}
