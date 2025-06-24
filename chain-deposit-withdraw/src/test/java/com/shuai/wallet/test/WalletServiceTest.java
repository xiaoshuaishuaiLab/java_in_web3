package com.shuai.wallet.test;

import com.shuai.wallet.WalletApplication;
import com.shuai.wallet.service.WalletService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.protocol.Web3j;

@SpringBootTest(classes = WalletApplication.class)
public class WalletServiceTest {

    @Resource
    private WalletService ETHWalletService;

    @Test
    public void testGetAddress() {
        for (int i = 0; i < 10; i++) {
            String address = ETHWalletService.getAddress(i);
            System.out.println(address);
        }
    }


}
