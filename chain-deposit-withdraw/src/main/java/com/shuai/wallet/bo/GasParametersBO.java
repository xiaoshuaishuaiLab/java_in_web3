package com.shuai.wallet.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.math.BigInteger;

@Data
@AllArgsConstructor
public class GasParametersBO {
    /**
     * 基础费用
     */
    private BigInteger baseFee;
    /**
     * 优先级费用（小费）
     */
    private BigInteger maxPriorityFeePerGas;
    /**
     * 最大费用
     */
    private BigInteger maxFeePerGas;
}
