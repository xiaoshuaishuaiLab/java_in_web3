package com.shuai.eth;

//import com.shuai.eth.notcommit.Constant;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.core.json.ReaderBasedJsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import io.vertx.core.json.Json;

public class Web3jDemo {

    private Web3j web3j;
    @Test
    public void test() {
        try {
            // 地址见 https://developer.metamask.io/key/active-endpoints
//            String url= "http://127.0.0.1:8545/";
//            String url= Constant.url;
//            String url= "https://mainnet.infura.io/v3/8c74af6f0b934262ac0c69351593a9ee";
//            String url= "https://testnet-rpc.monad.xyz";
//            String url= "https://api-hyperliquid-mainnet-evm.n.dwellir.com/0538a883-6c10-433a-aba1-7d15bcfc7b8d";
//            String url= "https://node.mainnet.etherlink.com";
//            String url= "https://rpc.duckchain.io";
//            String url= "https://mainnet-rpc.brisescan.com";
//            String url= "https://polygon-fullnode-qa.internal.nodereal.io";
//            String url= "https://polygon.drpc.org";
//            String url= "https://api.zan.top/polygon-mainnet";
//            String url= "https://polygon.lava.build";
//            String url= "https://polygon-public.nodies.app";
//            String url= "https://opbnb-mainnet.nodereal.io/v1/628fc3e869a7462f8a6f1992ba4e08d3";
//            String url= "https://bevm-fullnode-ap.internal.nodereal.io";
//            String url= "https://flare-fullnode-qa.internal.nodereal.io";
//            String url= "wss://polygon-bor-rpc.publicnode.com";
            String url= "https://eth-mainnet.nodereal.io/v1/560316491b114303a6113e7d6e790330";
//            String url= "https://bevm-fullnode-qa.internal.nodereal.io";


             web3j = Web3j.build(new HttpService(url));

            // 测试连接：获取最新区块号
            String blockNumber = web3j.ethBlockNumber().send().getBlockNumber().toString();
            System.out.println(" 最新区块号: " + blockNumber);



            //            web3j.ethCall();
            //        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/8c74af6f0b934262ac0c69351593a9ee"));

            //            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            //            System.out.println("clientVersion " + clientVersion);


            //            String gasPrice = web3j.ethGasPrice().send().getGasPrice().toString();
//            System.out.println("gasPrice: " + gasPrice);

            // 注意，此处的address要是外部账户（EOA）
//            BigInteger transactionCount = web3j.ethGetTransactionCount("0x6Cd01c0F55ce9E0Bf78f5E90f72b4345b16d515d", DefaultBlockParameterName.PENDING).send().getTransactionCount();
//            Optional<TransactionReceipt> transactionReceiptOptional = web3j.ethGetTransactionReceipt("0xc755bae08bbfd0c4f4ed6c77b5245587d29373d638bc32735055bd74eb968792").send().getTransactionReceipt();
//            if (transactionReceiptOptional.isPresent()) {
//                log.info("transactionReceipt = {}",transactionReceiptOptional.get());
//            }


            Request<?, EthBlock> ethBlockRequest = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(13934102), true);
            EthBlock.Block block = ethBlockRequest.send().getBlock();
            System.out.println("block = " + block);

            // 转换时间戳
            BigInteger timestampBigInt = block.getTimestamp();
            long decimalTimestamp = timestampBigInt.longValue();
            String hexTimestamp = "0x" + timestampBigInt.toString(16);
            System.out.println("十六进制时间戳: " + hexTimestamp);
            System.out.println("十进制时间戳: " + decimalTimestamp);
            System.out.println(new Date(decimalTimestamp * 1000L));
//            web3j.ethge
//            0x61d34068


//
//            ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
//            EthTransaction ethTransaction =
//                web3j.ethGetTransactionByHash("0xfbf5de5e55021d6dda54adbe6083082a758cf327194f6134e7bada440a02e672")
//                    .send();
//
//            System.out.println(ethTransaction.getTransaction().get());

//           String  ethTransactionJson = mapper.writeValueAsString(ethTransaction);
//            System.out.println(ethTransactionJson);
//
//            EthGetTransactionReceipt transactionReceipt =
//                web3j.ethGetTransactionReceipt("0xfbf5de5e55021d6dda54adbe6083082a758cf327194f6134e7bada440a02e672")
//                    .send();
//            System.out.println(transactionReceipt.getTransactionReceipt().get());


            //            web3j.ethSendTransaction()
//            wscat -c wss://mainnet.infura.io/ws/v3/8c74af6f0b934262ac0c69351593a9ee -x '{"jsonrpc": "2.0", "method": "eth_getBlockByNumber", "params": ["0x5BAD55", false], "id": 1}'
            // wscat -c wss://mainnet.infura.io/ws/v3/8c74af6f0b934262ac0c69351593a9ee -x '{"jsonrpc": "2.0", "id": 1, "method": "eth_subscribe", "params": ["newHeads"]}'

//            BigInteger polymarketBalance = getPolymarketBalance("0x4d97dcd97ec945f40cf65f87097ace5ea0476045",
//                "0x017BBD948248Be2ae58d8fbFaD186428C7291EF4",
//                new BigInteger("27343208495080017240945038136581882556126904612681189460291174036759416209886"));
//            System.out.println("polymarketBalance = " + polymarketBalance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 将十六进制字符串转换为十进制
     * @param hexString 十六进制字符串（可以带0x前缀或不带）
     * @return 十进制的长整型数值
     */
    public static long hexToDecimal(String hexString) {
        // 去除0x前缀（如果存在）
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }
        // 将十六进制转换为十进制
        return Long.parseLong(hexString, 16);
    }

    public BigInteger getPolymarketBalance(String contractAddress, String accountAddress, BigInteger tokenId) {
        try {
            Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(accountAddress), new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Uint256>() {})
            );
            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(accountAddress, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST
            ).send();

            List<Type> output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (output.isEmpty()) {
                return BigInteger.ZERO;
            }
            return (BigInteger) output.get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
