package com.shuai.wallet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "eth")
public class ETHConfig {

    /**
     * 以太坊节点 URL
     */
    private String url;
    private long chainId;
}