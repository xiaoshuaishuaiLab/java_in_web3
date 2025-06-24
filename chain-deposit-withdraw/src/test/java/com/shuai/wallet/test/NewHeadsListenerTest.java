package com.shuai.wallet.test;

import com.shuai.wallet.WalletApplication;
import com.shuai.wallet.listener.NewHeadsListener;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigInteger;

@SpringBootTest(classes = WalletApplication.class)
public class NewHeadsListenerTest {

    @Resource
    private NewHeadsListener newHeadsListener;

    @Qualifier("httpWeb3j")
    @Resource
    private Web3j web3j;


    @Test
    public void testProcessBlock() throws IOException {

        EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(8610208L)), true).send();
        newHeadsListener.processBlock(ethBlock);


    }
}
