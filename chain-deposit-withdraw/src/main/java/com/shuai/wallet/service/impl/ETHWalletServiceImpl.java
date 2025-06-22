package com.shuai.wallet.service.impl;

import com.shuai.wallet.config.ETHConfig;
import com.shuai.wallet.config.ETHWalletConfig;
import com.shuai.wallet.service.WalletService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.internal.ByteUtils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ETHWalletServiceImpl implements WalletService {

    private static final Network network = BitcoinNetwork.MAINNET;

    @Resource
    private ETHWalletConfig walletConfig;

    @Resource
    private ETHConfig ethConfig;
    @Autowired
    private ETHWalletConfig ethWalletConfig;
    @Autowired
    private ETHWalletConfig eTHWalletConfig;

    @Override
    public String getAddress(Integer childNum) {
        DeterministicKey xpubKey = DeterministicKey.deserializeB58(walletConfig.getMasterXPUB(), network);
        System.out.println("xpubKey路径: " + xpubKey.getPathAsString());
        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(xpubKey, new ChildNumber(childNum, false));
        String address = Numeric.prependHexPrefix(Keys.getAddress(childKey.getPublicKeyAsHex()));
        return address;
    }


    public DeterministicSeed genSeedWithEntropyAndPassphrase(String passphrase) {
        if (passphrase == null) {
            passphrase = "";
        }
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(eTHWalletConfig.getMnemonics(), passphrase);
        log.info("seed={} ", seed.toHexString());
        return seed;
    }

    // todo 派生出来的私钥和公钥可以保存起来，不用每次都生成
    // todo 看看web3j的org.web3j.crypto.Bip32ECKeyPair 实现下述逻辑
    public static Credentials  deriveUserCredentials(DeterministicSeed masterSeed, int userId) {
        DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(masterSeed).build();
        List<ChildNumber> path = Arrays.asList(
                new ChildNumber(44, true),
                new ChildNumber(60, true),
                new ChildNumber(0, true),
                new ChildNumber(0, false),
                new ChildNumber(userId, false)
        );
        DeterministicKey key = chain.getKeyByPath(path, true);
        System.out.println("Path: " + key.getPath());

        ECKeyPair ecKeyPair =  ECKeyPair.create(key.getPrivKey());
        Credentials credentials = Credentials.create(ecKeyPair);

        String checksumAddress = Keys.toChecksumAddress(credentials.getAddress());
        System.out.println("Checksum Address: " + checksumAddress);

        return credentials;
    }



    @Override
    public String signedTransaction(int fromAddressAccountIndex,long chainId, BigInteger nonce, BigInteger gasLimit, String to, BigInteger value, BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas) {
        try {

            DeterministicSeed seed = genSeedWithEntropyAndPassphrase(null);
            Credentials credentials = deriveUserCredentials(seed, fromAddressAccountIndex);
            RawTransaction transaction = RawTransaction.createEtherTransaction(
                    chainId,
                    nonce,
                    gasLimit, // gas limit for simple transfer
                    to,
                    value,
                    maxPriorityFeePerGas,
                    maxFeePerGas
            );
            byte[] bytes = TransactionEncoder.signMessage(transaction, credentials);
            String hexValue = Numeric.toHexString(bytes);
            return hexValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        0x02f87283aa36a780831158d68322b1ac8252089449dcde8b9e56dd9bd172002db5233c350cc7abf987038d7ea4c6800080c080a05fa6a4bb3cb8ac3da487503cf9ac7d9c306aec2703e4eaa885b98b798783ec47a024537c37b52b5a658848767eaa678913d9a273a86e75a4f3a42bbb552d80acb8
        return null;
    }

    public static void main(String[] args) {
        RawTransaction decode = TransactionDecoder.decode("0x02f87283aa36a780831158d68322b1ac8252089449dcde8b9e56dd9bd172002db5233c350cc7abf987038d7ea4c6800080c080a05fa6a4bb3cb8ac3da487503cf9ac7d9c306aec2703e4eaa885b98b798783ec47a024537c37b52b5a658848767eaa678913d9a273a86e75a4f3a42bbb552d80acb8");
        System.out.println(decode);

    }
}
