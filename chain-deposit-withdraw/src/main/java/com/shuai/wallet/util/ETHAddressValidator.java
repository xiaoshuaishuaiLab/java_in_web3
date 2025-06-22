package com.shuai.wallet.util;

import org.web3j.utils.Numeric;
import org.web3j.crypto.Keys;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ETHAddressValidator {

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
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public AddressType getAddressType() { return addressType; }
        public void setAddressType(AddressType addressType) { this.addressType = addressType; }

        public String getNormalizedAddress() { return normalizedAddress; }
        public void setNormalizedAddress(String normalizedAddress) { this.normalizedAddress = normalizedAddress; }

        public String getChecksumAddress() { return checksumAddress; }
        public void setChecksumAddress(String checksumAddress) { this.checksumAddress = checksumAddress; }

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