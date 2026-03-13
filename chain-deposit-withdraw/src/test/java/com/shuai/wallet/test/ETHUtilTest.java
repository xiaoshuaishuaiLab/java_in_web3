package com.shuai.wallet.test;

import com.shuai.wallet.constant.Constants;
import com.shuai.wallet.polymarket.ConditionalTokensHelper;
import com.shuai.wallet.util.ETHUtil;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

public class ETHUtilTest {

    //    Web3j web3j = Web3j.build(new HttpService("https://rpc.monad.xyz"));
    //    Web3j web3j = Web3j.build(new HttpService("https://mainnet.era.zksync.io"));
//    Web3j web3j = Web3j.build(new HttpService("https://mainnet.optimism.io"));
    Web3j web3j = Web3j.build(new HttpService("https://polygon-fullnode-qa.internal.nodereal.io"));

    @Test
    public void testValidateAddress() {
        // 测试各种地址
        String[] testAddresses = {"0xBEDEBf01d0410ABC658dC7DD45b1621D424441b2",  // 有效地址
            "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",  // 小写地址
            "0x0000000000000000000000000000000000000000",  // 零地址
            "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b",   // 长度错误
            "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8bG",  // 非法字符
            null,                                           // 空值
            ""                                             // 空字符串
        };

        for (String address : testAddresses) {
            System.out.println("Testing: " + address);
            ETHUtil.ValidationResult result = ETHUtil.validateAddress(address);
            System.out.println("Result: " + result);
            System.out.println("---");
        }
    }

    @Test
    public void test2() {
        String eventHash = ETHUtil.getEventHash(Constants.IncreaseObservationCardinalityNext);
        System.out.println("EventHash: " + eventHash);
    }

    @Test
    public void test3() {
        String eventHash;
        //        String eventHash = ETHUtil.getEventHash("Swap(address,address,int256,int256,uint160,uint128,int24,uint128,uint128)");
        //        System.out.println("EventHash: " + eventHash);
        //
        //         eventHash = ETHUtil.getEventHash("Mint(address,address,int24,int24,uint128,uint256,uint256)");
        //        System.out.println("EventHash: " + eventHash);
        //
        //        eventHash = ETHUtil.getEventHash("Burn(address,int24,int24,uint128,uint256,uint256)");
        //        System.out.println("EventHash: " + eventHash);

        //        event Burn(address indexed sender, uint amount0, uint amount1, address indexed to)

        //        eventHash = ETHUtil.getEventHash("Burn(address,uint256,uint256,address)");
        //        System.out.println("EventHash: " + eventHash);

        //        event Mint(address indexed sender, uint amount0, uint amount1);
        //        eventHash = ETHUtil.getEventHash("Mint(address,uint256,uint256)");
        //        System.out.println("EventHash: " + eventHash);

        //        eventHash = ETHUtil.getEventHash("Swapped(address,address,address,uint256,uint256,bytes)");
        //        System.out.println("EventHash: " + eventHash);
        //
        //        eventHash = ETHUtil.getEventHash("Deposited(address,uint256,uint256)");
        //        System.out.println("EventHash: " + eventHash);
        //
        //
        //        eventHash = ETHUtil.getEventHash("AssetWithdrawn(address,uint256,address,uint256)");
        //        System.out.println("EventHash: " + eventHash);
        //
        //        eventHash = ETHUtil.getEventHash("Withdrawn(address,uint256,uint256)");
        //        System.out.println("EventHash: " + eventHash);

        //        eventHash = ETHUtil.getEventHash("Mint(address,uint256,uint256,uint256,address)");
        //        System.out.println("EventHash: " + eventHash);
        //
        //        eventHash = ETHUtil.getEventHash("Burn(address,uint256,uint256,uint256,address)");
        //        System.out.println("EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("ConditionPreparation(bytes32,address,bytes32,uint256)");
        System.out.println("EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("QuestionInitialized(bytes32,uint256,address,bytes,address,uint256,uint256)");
        System.out.println("QuestionInitialized EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("PositionSplit(address,address,bytes32,bytes32,uint256[],uint256)");
        System.out.println("PositionSplit EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("TokenRegistered(uint256,uint256,bytes32)");
        System.out.println("TokenRegistered EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("ConditionPreparation(bytes32,address,bytes32,uint256)");
        System.out.println("EventHash: " + eventHash);


        eventHash = ETHUtil.getEventHash("OrderFilled(bytes32,address,address,uint256,uint256,uint256,uint256,uint256)");
        System.out.println("EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("PositionsMerge(address,address,bytes32,bytes32,uint256[],uint256)");
        System.out.println("EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("PositionsMerge(address,address,bytes32,bytes32,uint256[],uint256)");
        System.out.println("EventHash: " + eventHash);

        eventHash = ETHUtil.getEventHash("Burn(address,int24,int24,uint128,uint256,uint256)");
        System.out.println("EventHash: " + eventHash);

    }

    @Test
    public void testGetPositionId() {
        String collateralToken = "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174";
        String collectionIdHex = "0x0000000000000000000000000000000000000000000000000000000000000000";

        // 移除 0x 前缀并转换为字节数组
        String cleanHex = collectionIdHex.startsWith("0x") ? collectionIdHex.substring(2) : collectionIdHex;
        byte[] collectionId = ETHUtil.hexStringToByteArray(cleanHex);

        BigInteger positionId = ETHUtil.getPositionId(collateralToken, collectionId);
        System.out.println("Position ID: " + positionId);
        System.out.println("Position ID (hex): 0x" + positionId.toString(16));
    }

    @Test
    public void test4() {
        //        getERC20Balance("0x754704Bc059F8C67012fEd69BC8A327a5aafb603", "0x4cd00e387622c35bddb9b4c962c136462338bc31");
        getERC20Balance("0xe55bE555E8512Ff0B821151A0EF94b6d9C3A0A6E", "0xd0f06841076ac458529510be14e2363aee577f0a");

    }

    @Test
    public void test5() {
        ArrayList<String> list = Lists.newArrayList("0xfe4efed145508aeb124152f18b4035c01b22d016");
        list.stream().forEach(e -> System.out.println(getToken0(e)));
    }

    @Test
    public void test6() {
        getMaster("0xcad270b1eed428260310c632edd737128bc10667");
    }

    public String getMaster(String contractAddress) {

        try {
            Function function =
                new Function("master", List.of(), Collections.singletonList(new TypeReference<Address>() {
                }));
            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", contractAddress,
                    encodedFunction), DefaultBlockParameterName.LATEST).send();

            // 检查是否有错误
            if (response.hasError()) {
                System.err.println("Error: " + response.getError().getMessage());
                System.err.println("Error code: " + response.getError().getCode());
                System.err.println("Error data: " + response.getError().getData());
                return null;
            }

            List<Type> output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (output.isEmpty()) {
                return null;
            }
            // token0返回的是地址，应该转换为String
            return (String)output.get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getToken0(String contractAddress) {

        try {
            Function function =
                new Function("token0", List.of(), Collections.singletonList(new TypeReference<Address>() {
                }));
            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction("0x0000000000000000000000000000000000000000", contractAddress,
                    encodedFunction), DefaultBlockParameterName.LATEST).send();

            // 检查是否有错误
            if (response.hasError()) {
                System.err.println("Error: " + response.getError().getMessage());
                System.err.println("Error code: " + response.getError().getCode());
                System.err.println("Error data: " + response.getError().getData());
                return null;
            }

            List<Type> output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (output.isEmpty()) {
                return null;
            }
            // token0返回的是地址，应该转换为String
            return (String)output.get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public BigInteger getERC20Balance(String contractAddress, String accountAddress) {

        try {
            Function function = new Function("balanceOf", Arrays.asList(new Address(accountAddress)),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response =
                web3j.ethCall(Transaction.createEthCallTransaction(accountAddress, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST).send();

            List<Type> output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (output.isEmpty()) {
                return BigInteger.ZERO;
            }
            return (BigInteger)output.get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testGetCollectionId() {
        String collectionId = getCollectionId("0x4d97dcd97ec945f40cf65f87097ace5ea0476045",
            "0000000000000000000000000000000000000000000000000000000000000000",
            "81E33CA5BD2F1C4743E88FB0741BA512111708946A6F5CC4A158EA6C01FDABF8", "1");
        System.out.println(collectionId);

        String collectionId2 =
            ConditionalTokensHelper.getCollectionId("0000000000000000000000000000000000000000000000000000000000000000",
                "81E33CA5BD2F1C4743E88FB0741BA512111708946A6F5CC4A158EA6C01FDABF8", new BigInteger("1"));

        System.out.println(collectionId2);


        //        0x57be06e3b010f6981762060c7e1f95773b5662c42c8008a0a64fffed36f083d0
    }

//    BigInteger positionId =
//        ConditionalTokensHelper.getPositionId("0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174", collectionId);
//        System.out.println(positionId);
    /**
     * 调用链上合约的 getCollectionId 方法
     * @param contractAddress 合约地址
     * @param parentCollectionId 父集合ID（bytes32格式的十六进制字符串，可带或不带0x前缀）
     * @param conditionId 条件ID（bytes32格式的十六进制字符串，可带或不带0x前缀）
     * @param indexSet 索引集合（十六进制字符串或十进制数字字符串）
     * @return 返回 collectionId（bytes32格式的十六进制字符串，带0x前缀）
     */
    public String getCollectionId(String contractAddress, String parentCollectionId, String conditionId, String indexSet) {
        try {
            // 处理 parentCollectionId: 移除 0x 前缀（如果有）并转换为 byte[32]
            String cleanParentId = parentCollectionId.startsWith("0x") ? parentCollectionId.substring(2) : parentCollectionId;
            byte[] parentIdBytes = new byte[32];
            byte[] tempParent = hexStringToByteArray(cleanParentId);
            System.arraycopy(tempParent, 0, parentIdBytes, 32 - tempParent.length, tempParent.length);

            // 处理 conditionId: 移除 0x 前缀（如果有）并转换为 byte[32]
            String cleanConditionId = conditionId.startsWith("0x") ? conditionId.substring(2) : conditionId;
            byte[] conditionIdBytes = new byte[32];
            byte[] tempCondition = hexStringToByteArray(cleanConditionId);
            System.arraycopy(tempCondition, 0, conditionIdBytes, 32 - tempCondition.length, tempCondition.length);

            // 处理 indexSet: 可能是十六进制或十进制字符串
            BigInteger indexSetValue;
            if (indexSet.startsWith("0x")) {
                indexSetValue = new BigInteger(indexSet.substring(2), 16);
            } else {
                indexSetValue = new BigInteger(indexSet);
            }

            // 构建函数调用
            Function function = new Function(
                "getCollectionId",
                Arrays.asList(
                    new Bytes32(parentIdBytes),
                    new Bytes32(conditionIdBytes),
                    new Uint256(indexSetValue)
                ),
                Collections.singletonList(new TypeReference<Bytes32>() {})
            );

            String encodedFunction = FunctionEncoder.encode(function);

            // 执行调用
            EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    "0x0000000000000000000000000000000000000000",
                    contractAddress,
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send();

            // 检查错误
            if (response.hasError()) {
                System.err.println("Error: " + response.getError().getMessage());
                System.err.println("Error code: " + response.getError().getCode());
                System.err.println("Error data: " + response.getError().getData());
                return null;
            }

            // 解码返回值
            List<Type> output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (output.isEmpty()) {
                return null;
            }

            // bytes32 返回值转换为十六进制字符串
            byte[] resultBytes = (byte[]) output.get(0).getValue();
            StringBuilder hexString = new StringBuilder("0x");
            for (byte b : resultBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 辅助方法：将十六进制字符串转换为字节数组
     */
    private byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
