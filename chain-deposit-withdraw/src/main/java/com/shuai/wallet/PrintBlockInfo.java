package com.shuai.wallet;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class PrintBlockInfo {
    public static void main(String[] args) throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://sepolia.infura.io/v3/8c74af6f0b934262ac0c69351593a9ee"));
        EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, true).send();
        EthBlock.Block block = ethBlock.getBlock();

        printBlockInfoAsJsonLine(block);

//        // 推荐：完整JSON打印
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        String json = mapper.writeValueAsString(block);
//        System.out.println(json);
//
//        // 或者：手动打印主要字段
//        System.out.println("Block Number: " + block.getNumber());
//        System.out.println("Block Hash: " + block.getHash());
        // ... 其他字段
    }

    public static void printBlockInfoAsJsonLine(EthBlock.Block block) throws Exception {
        Map<String, Object> blockInfo = new LinkedHashMap<>();
        blockInfo.put("number", block.getNumberRaw());
        blockInfo.put("hash", block.getHash());
        blockInfo.put("parentHash", block.getParentHash());
        blockInfo.put("nonce", block.getNonceRaw());
        blockInfo.put("miner", block.getMiner());
        blockInfo.put("difficulty", block.getDifficultyRaw());
        // blockInfo.put("totalDifficulty", block.getTotalDifficultyRaw()); // 不加
        blockInfo.put("gasLimit", block.getGasLimitRaw());
        blockInfo.put("gasUsed", block.getGasUsedRaw());
        blockInfo.put("timestamp", block.getTimestampRaw());
        blockInfo.put("baseFeePerGas", block.getBaseFeePerGasRaw());
        blockInfo.put("extraData", block.getExtraData());
        blockInfo.put("size", block.getSizeRaw());
        blockInfo.put("stateRoot", block.getStateRoot());
        blockInfo.put("receiptsRoot", block.getReceiptsRoot());
        blockInfo.put("transactionsRoot", block.getTransactionsRoot());
        blockInfo.put("transactionsCount", block.getTransactions().size());
        blockInfo.put("uncles", block.getUncles());
        blockInfo.put("withdrawals", block.getWithdrawals());
        blockInfo.put("sealFields", block.getSealFields());
        blockInfo.put("author", block.getAuthor());
        blockInfo.put("mixHash", block.getMixHash());
        blockInfo.put("logsBloom", block.getLogsBloom());
        blockInfo.put("sha3Uncles", block.getSha3Uncles());
        blockInfo.put("withdrawalsRoot", block.getWithdrawalsRoot());
        blockInfo.put("blobGasUsed", block.getBlobGasUsedRaw());
        blockInfo.put("excessBlobGas", block.getExcessBlobGasRaw());

        // 详细打印 transactions
        ObjectMapper mapper = new ObjectMapper();
        // 遍历所有交易，转为 Map
        List<Object> txList = new java.util.ArrayList<>();
        for (EthBlock.TransactionResult<?> txResult : block.getTransactions()) {
            Object txObj = txResult.get();
            if (txObj instanceof EthBlock.TransactionObject) {
                EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txObj;
                Map<String, Object> txMap = new LinkedHashMap<>();
                txMap.put("hash", tx.getHash());
                txMap.put("nonce", tx.getNonceRaw());
                txMap.put("blockHash", tx.getBlockHash());
                txMap.put("blockNumber", tx.getBlockNumberRaw());
                txMap.put("transactionIndex", tx.getTransactionIndexRaw());
                txMap.put("from", tx.getFrom());
                txMap.put("to", tx.getTo());
                txMap.put("value", tx.getValue());
                txMap.put("gasPrice", tx.getGasPriceRaw());
                txMap.put("gas", tx.getGasRaw());
//                txMap.put("input", tx.getInput());
                txMap.put("creates", tx.getCreates());
                txMap.put("publicKey", tx.getPublicKey());
                txMap.put("raw", tx.getRaw());
//                txMap.put("r", tx.getR());
//                txMap.put("s", tx.getS());
//                txMap.put("v", tx.getV());
                txMap.put("transactionType", tx.getType());
                txMap.put("maxFeePerGas", tx.getMaxFeePerGasRaw());
                txMap.put("maxPriorityFeePerGas", tx.getMaxPriorityFeePerGasRaw());
                txMap.put("accessList", tx.getAccessList());
                txMap.put("chainId", tx.getChainId());
                txMap.put("yParity", tx.getyParity());
                txList.add(txMap);
            }
        }
        blockInfo.put("transactions", txList);

        String json = mapper.writeValueAsString(blockInfo);
        System.out.println(json);
    }
}