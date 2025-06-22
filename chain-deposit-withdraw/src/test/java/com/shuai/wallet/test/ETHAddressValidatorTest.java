package com.shuai.wallet.test;

import com.shuai.wallet.util.ETHAddressValidator;
import org.junit.jupiter.api.Test;

public class ETHAddressValidatorTest {

    @Test
    public void test() {
        // 测试各种地址
        String[] testAddresses = {
                "0xBEDEBf01d0410ABC658dC7DD45b1621D424441b2",  // 有效地址
                "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b6",  // 小写地址
                "0x0000000000000000000000000000000000000000",  // 零地址
                "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8b",   // 长度错误
                "0x742d35Cc6634C0532925a3b8D4C9db96C4b4d8bG",  // 非法字符
                null,                                           // 空值
                ""                                             // 空字符串
        };

        for (String address : testAddresses) {
            System.out.println("Testing: " + address);
            ETHAddressValidator.ValidationResult result = ETHAddressValidator.validateAddress(address);
            System.out.println("Result: " + result);
            System.out.println("---");
        }
    }
}
