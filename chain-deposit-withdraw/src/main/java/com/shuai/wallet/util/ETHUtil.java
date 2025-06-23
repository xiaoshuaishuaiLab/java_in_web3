package com.shuai.wallet.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;
import org.web3j.crypto.Keys;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ETHUtil {
    static ObjectMapper mapper = new ObjectMapper();

    /**
     * 最完整的以太坊地址验证
     * 包含：格式验证、校验和验证、零地址检查、合约地址检查等
     */
    public static ValidationResult validateAddress(String address) {
        ValidationResult result = new ValidationResult();

        // 1. 空值检查
        if (address == null || address.trim().isEmpty()) {
            result.setValid(false);
            result.setError("Address cannot be null or empty");
            return result;
        }

        // 2. 基本格式验证
        if (!isValidFormat(address)) {
            result.setValid(false);
            result.setError("Invalid address format");
            return result;
        }

        // 3. 校验和验证
        if (!isValidChecksum(address)) {
            result.setValid(false);
            result.setError("Invalid checksum address");
            return result;
        }

        // 4. 零地址检查
        if (isZeroAddress(address)) {
            result.setValid(false);
            result.setError("Zero address is not allowed");
            return result;
        }

        // 5. 地址类型分析
        result.setAddressType(getAddressType(address));

        // 6. 地址规范化和标准化
        result.setNormalizedAddress(normalizeAddress(address));
        result.setChecksumAddress(Keys.toChecksumAddress(address.toLowerCase()));

        result.setValid(true);
        return result;
    }

    public static String AddressRecoveryFromTransaction(String signedTxHex) {
        try {
            // 1. Decode the raw transaction hex
            // This parses the hex string into a structured transaction object
            SignedRawTransaction signedTx = (SignedRawTransaction) TransactionDecoder.decode(signedTxHex);

            // 2. Recover the sender's address from the signature
            // The .getFrom() method performs the ecrecover operation internally
            String senderAddress = signedTx.getFrom();

            System.out.println("Successfully recovered the sender's address.");
            System.out.println("Sender Address: " + senderAddress);

            // For your specific transaction, this will print:
            // Sender Address: 0x03742456a023a1d799abe0b6955e9a68344e43f1
            return senderAddress;
        } catch (Exception e) {
            System.err.println("Failed to decode transaction or recover address: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }


    public static void printBlockInfoAsJsonLine(EthBlock.Block block) {
        try {
            Map<String, Object> blockInfo = new LinkedHashMap<>();
            blockInfo.put("number", block.getNumber());
            blockInfo.put("hash", block.getHash());
            blockInfo.put("parentHash", block.getParentHash());
            blockInfo.put("nonce", block.getNonce());
            blockInfo.put("miner", block.getMiner());
//            blockInfo.put("difficulty", block.getDifficultyRaw());
            // blockInfo.put("totalDifficulty", block.getTotalDifficultyRaw()); // 不加
            blockInfo.put("gasLimit", block.getGasLimit());
            blockInfo.put("gasUsed", block.getGasUsed());
            blockInfo.put("timestamp", block.getTimestamp());
            blockInfo.put("baseFeePerGas", block.getBaseFeePerGas());
            blockInfo.put("extraData", block.getExtraData());
            blockInfo.put("size", block.getSize());
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
            // 遍历所有交易，转为 Map
            List<Object> txList = new java.util.ArrayList<>();
            for (EthBlock.TransactionResult<?> txResult : block.getTransactions()) {
                Object txObj = txResult.get();
                if (txObj instanceof EthBlock.TransactionObject) {
                    EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txObj;
                    Map<String, Object> txMap = new LinkedHashMap<>();
                    txMap.put("hash", tx.getHash());
                    txMap.put("nonce", tx.getNonce());
                    txMap.put("blockHash", tx.getBlockHash());
                    txMap.put("blockNumber", tx.getBlockNumber());
                    txMap.put("transactionIndex", tx.getTransactionIndex());
                    txMap.put("from", tx.getFrom());
                    txMap.put("to", tx.getTo());
                    txMap.put("value", tx.getValueRaw());
                    txMap.put("gasPrice", tx.getGasPrice());
                    txMap.put("gas", tx.getGas());
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

            log.info("blockInfo = {}", mapper.writeValueAsString(blockInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 基本格式验证
     */
    private static boolean isValidFormat(String address) {
        // 检查前缀
        if (!address.startsWith("0x")) {
            return false;
        }

        // 检查长度（0x + 40位16进制 = 42位）
        if (address.length() != 42) {
            return false;
        }

        // 检查是否都是16进制字符
        String hexPart = address.substring(2);
        return hexPart.matches("^[0-9a-fA-F]{40}$");
    }

    /**
     * 校验和验证（EIP-55）
     */
    private static boolean isValidChecksum(String address) {
        try {
            // 如果地址已经是校验和格式，验证其正确性
            String expectedChecksum = Keys.toChecksumAddress(address);
            return address.equals(expectedChecksum);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 零地址检查
     */
    private static boolean isZeroAddress(String address) {
        String cleanAddress = Numeric.cleanHexPrefix(address).toLowerCase();
        return cleanAddress.equals("0000000000000000000000000000000000000000");
    }

    /**
     * 获取地址类型
     */
    private static AddressType getAddressType(String address) {
        String cleanAddress = Numeric.cleanHexPrefix(address).toLowerCase();

        // 零地址
        if (cleanAddress.equals("0000000000000000000000000000000000000000")) {
            return AddressType.ZERO_ADDRESS;
        }

        // 预编译合约地址（0x0000000000000000000000000000000000000001 到 0x0000000000000000000000000000000000000008）
        if (cleanAddress.matches("^000000000000000000000000000000000000000[1-8]$")) {
            return AddressType.PRECOMPILED_CONTRACT;
        }

        // 检查是否为可能的合约地址（通过地址特征判断）
        // 注意：这只是推测，实际需要查询区块链状态
        if (isLikelyContractAddress(address)) {
            return AddressType.LIKELY_CONTRACT;
        }

        return AddressType.EOA; // 外部拥有账户
    }

    /**
     * 推测是否为合约地址（基于地址特征）
     * 注意：这只是推测，实际需要查询区块链状态
     */
    private static boolean isLikelyContractAddress(String address) {
        String cleanAddress = Numeric.cleanHexPrefix(address).toLowerCase();

        // 一些常见的合约地址特征（这只是示例）
        // 实际应用中应该查询区块链状态
        return cleanAddress.startsWith("000000") ||
                cleanAddress.startsWith("111111") ||
                cleanAddress.startsWith("222222");
    }

    /**
     * 地址规范化（转小写）
     */
    private static String normalizeAddress(String address) {
        return address.toLowerCase();
    }


    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private String error;
        private AddressType addressType;
        private String normalizedAddress;
        private String checksumAddress;

        // Getters and Setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public AddressType getAddressType() {
            return addressType;
        }

        public void setAddressType(AddressType addressType) {
            this.addressType = addressType;
        }

        public String getNormalizedAddress() {
            return normalizedAddress;
        }

        public void setNormalizedAddress(String normalizedAddress) {
            this.normalizedAddress = normalizedAddress;
        }

        public String getChecksumAddress() {
            return checksumAddress;
        }

        public void setChecksumAddress(String checksumAddress) {
            this.checksumAddress = checksumAddress;
        }

        @Override
        public String toString() {
            if (valid) {
                return String.format("Valid address: %s (Type: %s)", checksumAddress, addressType);
            } else {
                return String.format("Invalid address: %s", error);
            }
        }
    }

    /**
     * 地址类型枚举
     */
    public enum AddressType {
        EOA("External Owned Account"),
        LIKELY_CONTRACT("Likely Contract Address"),
        PRECOMPILED_CONTRACT("Precompiled Contract"),
        ZERO_ADDRESS("Zero Address");

        private final String description;

        AddressType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

}