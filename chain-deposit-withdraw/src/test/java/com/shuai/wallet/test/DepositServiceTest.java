package com.shuai.wallet.test;

import com.shuai.wallet.WalletApplication;
import com.shuai.wallet.service.DepositService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthTransaction;

import java.io.IOException;

@SpringBootTest(classes = WalletApplication.class)
public class DepositServiceTest {

    @Resource
    private DepositService depositService;

    @Qualifier("httpWeb3j")
    @Resource
    private Web3j web3j;


    @Test
    public void testUSDTDeposit() throws IOException {

        EthTransaction send = web3j.ethGetTransactionByHash("0x009c5600ab02b3e39755b5ae2ffaa1fb7fa2391bcde2cbba0427702c90ce68a8").send();
        depositService.processDeposit(send.getTransaction().get());

    }
}
