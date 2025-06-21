package com.shuai.wallet.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurrencyCodeEnum {

    BTC("BTC"),
    ETH("ETH"),
    USDT("USDT"),
    ;

    String code;

}
