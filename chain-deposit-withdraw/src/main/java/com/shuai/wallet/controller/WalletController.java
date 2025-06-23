package com.shuai.wallet.controller;

import com.shuai.wallet.bo.GasParametersBO;
import com.shuai.wallet.config.ETHConfig;
import com.shuai.wallet.enums.CurrencyCodeEnum;
import com.shuai.wallet.service.GasService;
import com.shuai.wallet.service.WalletService;
import com.shuai.wallet.util.ETHUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@RestController
public class WalletController {

    @Resource
    private WalletService ETHWalletService;
    @Resource
    private GasService gasService;

    @Qualifier("httpWeb3j")
    @Resource
    private Web3j web3j;

    @Resource
    private ETHConfig ethConfig;

    // "ETH", "BTC", "USDT")
    // uid = 1 的 adress 是 0x6e575dbC4C359bcB6b5cb59756b64De8aB2BA66c  metamask的地址都是 checkSumAddress
    // sepolia 的 USDT address is  0xaa8e23fb1079ea71e0a56f48a2aa51851d8433d0
    @GetMapping("user/depositAddress")
    public String getDepositAddress(@RequestParam Integer uid, @RequestParam String currencyCode) {
        Integer childNum = getChildNumByUid(uid);
        try {
            if (currencyCode.equals(CurrencyCodeEnum.ETH.getCode()) || currencyCode.equals(CurrencyCodeEnum.USDT.getCode())) {
                String address = ETHWalletService.getAddress(childNum);
                return address;
            } else {
                // todo btc
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("getDepositAddress, currencyCode: {}", currencyCode);
        return null;
    }

    /**
     * @param uid
     * @param currencyCode
     * @param amountStr    数量 wei
     * @param toAddress    用户个人的链上地址
     * @return
     */
    @PostMapping("user/withdraw")
    public String withdraw(@RequestParam Integer uid, @RequestParam String currencyCode, @RequestParam String amountStr, @RequestParam String toAddress) {
        if (!currencyCode.equals(CurrencyCodeEnum.ETH.getCode()) && !currencyCode.equals(CurrencyCodeEnum.USDT.getCode())) {
            throw new RuntimeException("not support currency code");
        }
        // 校验地址是否符合规范
        ETHUtil.ValidationResult validationResult = ETHUtil.validateAddress(toAddress);
        if (!validationResult.isValid()) {
            throw new RuntimeException(validationResult.getError());
        }

        // 校验用户的余额 略
        // 冻结用户的余额 略
        // 校验平台某个钱包(fromAddress 暂时写死)的余额，暂从infura上获取
        try {
            String fromAddress = "0x6e575dbC4C359bcB6b5cb59756b64De8aB2BA66c";
            int fromAddressAccountIndex = 1;

            if (currencyCode.equals(CurrencyCodeEnum.ETH.getCode())) {
                BigInteger value = Convert.toWei(BigDecimal.valueOf(Double.parseDouble(amountStr)), Convert.Unit.ETHER).toBigInteger();

                BigInteger balance = web3j.ethGetBalance(fromAddress, DefaultBlockParameterName.LATEST).send().getBalance();
                if (balance.compareTo(value) < 0) {
                    throw new RuntimeException("此钱包余额不足");
                }
                // 获取nonce
                BigInteger nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).send().getTransactionCount();
                GasParametersBO gasParams = gasService.getGasParameters();
                if (ObjectUtils.isEmpty(gasParams)) {
                    throw new RuntimeException("gasParams empty");
                }
                String signedTransactionData = ETHWalletService.signedTransaction(fromAddressAccountIndex, ethConfig.getChainId(), nonce, toAddress, value, gasParams.getMaxPriorityFeePerGas(), gasParams.getMaxFeePerGas());

                EthSendTransaction send = web3j.ethSendRawTransaction(signedTransactionData).send();
                log.info("send = {}", send);
                if (send.hasError()) {
                    throw new RuntimeException("withdraw error");
                }
            } else if (currencyCode.equals(CurrencyCodeEnum.USDT.getCode())) {
                String usdtContractAddress = "0xaa8e23fb1079ea71e0a56f48a2aa51851d8433d0";
                BigInteger erc20Balance = ETHWalletService.getERC20Balance(usdtContractAddress, fromAddress);
                BigInteger value = Convert.toWei(BigDecimal.valueOf(Double.parseDouble(amountStr)), Convert.Unit.MWEI).toBigInteger();
                if (erc20Balance.compareTo(value) < 0) {
                    throw new RuntimeException("此钱包余额不足");
                }
                // usdt 发起转账
                // 获取nonce
                GasParametersBO gasParams = gasService.getGasParameters();
                if (ObjectUtils.isEmpty(gasParams)) {
                    throw new RuntimeException("gasParams empty");
                }

                ETHWalletService.sendUSDT(fromAddressAccountIndex,ethConfig.getChainId(),usdtContractAddress,toAddress,value,gasParams.getMaxPriorityFeePerGas(),gasParams.getMaxFeePerGas());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 发起转账
        return null;
    }


    private Integer getChildNumByUid(Integer uid) {
        return uid;
    }


}
