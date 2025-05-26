package com.shuai.bitcoin.mastering;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class TransactionExampleTest {

    @Resource
    private TransactionExample transactionExample;

    @Test
    void getTransactionById() {
        for (int i = 0; i < 10; i++) {
            try {
                transactionExample.getTransactionById("0627052b6f28912f2703066a912ea577f2ce4da4caa5a5fbd8a57286c345c2f2");
//                transactionExample.getTransactionById("2601f5fb4e31fc554b218dc510e97c3d0eb4e2cd1638b4b7152f9218f7f93edf");
            } catch (Exception e) {
                log.info("getTransactionById error",e);
            }
        }
        log.info("done");
    }

}
