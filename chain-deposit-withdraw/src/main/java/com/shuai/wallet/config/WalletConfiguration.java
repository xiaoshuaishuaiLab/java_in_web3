package com.shuai.wallet.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;

import java.net.ConnectException;

@Configuration
public class WalletConfiguration {

    @Resource
    private ETHConfig ethConfig;


    @Bean(name = "httpWeb3j")
    public Web3j web3j() {
        Web3j web3j = Web3j.build(new HttpService(ethConfig.getUrl()));
        return web3j;
    }

    @Bean(name = "wsWeb3j")
    public Web3j web3jWebSocket() throws ConnectException {
        WebSocketService webSocketService = new WebSocketService(ethConfig.getWsUrl(), true);
        webSocketService.connect();
        Web3j web3j = Web3j.build(webSocketService);
        return web3j;
    }
}
