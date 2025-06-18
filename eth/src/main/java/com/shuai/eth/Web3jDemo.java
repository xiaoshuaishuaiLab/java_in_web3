package com.shuai.eth;

import com.shuai.eth.notcommit.Constant;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

public class Web3jDemo {

    @Test
    public void test() {
        try {
            // 地址见 https://developer.metamask.io/key/active-endpoints
//            String url= "http://127.0.0.1:8545/";
            String url= Constant.url;
//            String url= "https://mainnet.infura.io/v3/8c74af6f0b934262ac0c69351593a9ee";

            Web3j web3j = Web3j.build(new HttpService(url));
//        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/8c74af6f0b934262ac0c69351593a9ee"));

//            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
//            System.out.println("clientVersion " + clientVersion);

            // 测试连接：获取最新区块号
//            String blockNumber = web3j.ethBlockNumber().send().getBlockNumber().toString();
//            System.out.println("Sepolia 最新区块号: " + blockNumber);

//            String gasPrice = web3j.ethGasPrice().send().getGasPrice().toString();
//            System.out.println("gasPrice: " + gasPrice);

            // 注意，此处的address要是外部账户（EOA）
//            BigInteger transactionCount = web3j.ethGetTransactionCount("0x6Cd01c0F55ce9E0Bf78f5E90f72b4345b16d515d", DefaultBlockParameterName.PENDING).send().getTransactionCount();
//            Optional<TransactionReceipt> transactionReceiptOptional = web3j.ethGetTransactionReceipt("0xc755bae08bbfd0c4f4ed6c77b5245587d29373d638bc32735055bd74eb968792").send().getTransactionReceipt();
//            if (transactionReceiptOptional.isPresent()) {
//                log.info("transactionReceipt = {}",transactionReceiptOptional.get());
//            }
            Request<?, EthBlock> ethBlockRequest = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(8573099), true);
            EthBlock.Block block = ethBlockRequest.send().getBlock();
            System.out.println("ethBlockRequest = " + block);
//            web3j.ethSendTransaction()
//            wscat -c wss://mainnet.infura.io/ws/v3/8c74af6f0b934262ac0c69351593a9ee -x '{"jsonrpc": "2.0", "method": "eth_getBlockByNumber", "params": ["0x5BAD55", false], "id": 1}'
            // wscat -c wss://mainnet.infura.io/ws/v3/8c74af6f0b934262ac0c69351593a9ee -x '{"jsonrpc": "2.0", "id": 1, "method": "eth_subscribe", "params": ["newHeads"]}'
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
