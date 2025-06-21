package com.shuai.wallet.controller;

import com.shuai.wallet.enums.CurrencyCodeEnum;
import com.shuai.wallet.service.WalletService;
import jakarta.annotation.Resource;
import jnr.ffi.annotations.In;
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

    @Resource
    private WalletService ETHWalletService;


    // "ETH", "BTC", "USDT")
    // uid = 1 的 adress 是 0xbedebf01d0410abc658dc7dd45b1621d424441b2
    @GetMapping("user/depositAddress")
    public String getDepositAddress(@RequestParam Integer uid, @RequestParam String currencyCode) {
        Integer childNum = getChildNumByUid(uid);
        try {
            if (currencyCode.equals(CurrencyCodeEnum.ETH.getCode()) || currencyCode.equals(CurrencyCodeEnum.USDT.getCode())) {
                String address = ETHWalletService.getAddress(childNum);
                return address;
            } else {
                // todo btc
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("getDepositAddress, currencyCode: {}", currencyCode);
        return null;
    }




    private Integer getChildNumByUid(Integer uid) {
        return uid;
    }


}
