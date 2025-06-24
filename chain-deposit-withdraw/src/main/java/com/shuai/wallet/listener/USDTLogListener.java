package com.shuai.wallet.listener;

import com.shuai.wallet.config.ETHConfig;
import com.shuai.wallet.constant.Constants;
import com.shuai.wallet.util.ETHUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;

@Slf4j
@Service
public class USDTLogListener {

    @Qualifier("wsWeb3j")
    @Resource
    private Web3j wsWeb3j;


    @Resource
    private ETHConfig ethConfig;

    @PostConstruct
    public void start() {

        String usdtContract = ethConfig.getUsdtContract();
        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                usdtContract
        );
        // Transfer事件topic
//        filter.addSingleTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");
        String topic = ETHUtil.getEventHash(Constants.ERC_20_TRANSFER_EVENT_SIGNATURE);
        log.info("topic : {}", topic);
        filter.addSingleTopic(topic);

        // 处理事件
        wsWeb3j.ethLogFlowable(filter).subscribe(
                this::processTransferEvent,
                (Throwable error) -> {
                    log.error("WebSocket 订阅日志出错: {}", error.getMessage(), error);
                }
        );
    }

    // usdt的处理可以通过监听转账事件来监听，可以把com.shuai.wallet.listener.NewHeadsListener.processBlockLogs的逻辑迁移到这里
   private void processTransferEvent(Log transferLog) {
        log.info("transferLog : {}", transferLog);
    }
}
