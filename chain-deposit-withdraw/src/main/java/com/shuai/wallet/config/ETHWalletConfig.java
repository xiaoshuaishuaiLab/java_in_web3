package com.shuai.wallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "eth-wallet")
public class ETHWalletConfig {
    private List<String> mnemonics;
    private String masterXPUB;
}

