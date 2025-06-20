package com.shuai.wallet.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.WalletUtils;
import org.web3j.crypto.exception.CipherException;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Slf4j
@RestController
public class WalletController {


    // "ETH", "BTC", "USDT")
    @GetMapping("user/depositAddress")
    public String getDepositAddress(@RequestParam String currencyCode) {
        try {
            // 非确定钱包的逻辑不好，应该换成hd钱包
//            File keystoreDir = new File("/data/eth/data/keystore");
//            if (!keystoreDir.exists()) {
//                keystoreDir.mkdirs();
//            }
//            String fileName = WalletUtils.generateNewWalletFile("", keystoreDir, true);
//            log.info("getDepositAddress, fileName: {}", fileName);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        log.info("getDepositAddress, currencyCode: {}", currencyCode);
        return null;
    }

}
