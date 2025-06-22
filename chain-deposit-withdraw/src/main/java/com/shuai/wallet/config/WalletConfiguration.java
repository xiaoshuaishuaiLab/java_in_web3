package com.shuai.wallet.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class WalletConfiguration {

    @Resource
    private ETHConfig ethConfig;


    @Bean
    public Web3j web3j() {
        Web3j web3j = Web3j.build(new HttpService(ethConfig.getUrl()));
        return web3j;
    }
}
