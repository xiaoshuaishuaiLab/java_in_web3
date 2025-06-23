package com.shuai.wallet.service.impl;

import com.shuai.wallet.bo.GasParametersBO;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ETHWalletServiceImpl implements WalletService {

    private static final Network network = BitcoinNetwork.MAINNET;

    @Resource
    private ETHWalletConfig walletConfig;

    @Resource
    private ETHConfig ethConfig;

    @Qualifier("httpWeb3j")
    @Resource
    private Web3j web3j;

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
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(walletConfig.getMnemonics(), passphrase);
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
    public String signedTransaction(int fromAddressAccountIndex,long chainId, BigInteger nonce, String to, BigInteger value, BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas) {
        try {
            DeterministicSeed seed = genSeedWithEntropyAndPassphrase(null);
            Credentials credentials = deriveUserCredentials(seed, fromAddressAccountIndex);
            RawTransaction transaction = RawTransaction.createEtherTransaction(chainId,
                    nonce,
                    BigInteger.valueOf(21000), // gas limit for simple transfer
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
        return null;
    }

    @Override
    public String sendUSDT(int fromAddressAccountIndex,long chainId,String usdtContract, String toAddress, BigInteger amount, BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas) {
        try {
            DeterministicSeed seed = genSeedWithEntropyAndPassphrase(null);
            Credentials credentials = deriveUserCredentials(seed, fromAddressAccountIndex);
            BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send().getTransactionCount();
            // 2. 构造ERC20 transfer方法的data
            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(toAddress), new Uint256(amount)),
                    Arrays.asList()
            );
            String encodedFunction = FunctionEncoder.encode(function);
            // 3. 构造EIP-1559 RawTransaction
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    new BigInteger("60000"),
                    usdtContract,
                    BigInteger.ZERO, // 主币转账为0
                    encodedFunction,
                    maxPriorityFeePerGas,
                    maxFeePerGas
            );
            // 4. 签名
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            // 5. 广播
            EthSendTransaction send = web3j.ethSendRawTransaction(hexValue).send();
            log.info("EthSendTransaction = {}",send);

            if (send.hasError()) {
                throw new RuntimeException("USDT转账失败: " + send.getError().getMessage());
            }
            return send.getTransactionHash();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    public  BigInteger getERC20Balance(String contractAddress, String accountAddress) {
        try {
            Function function = new Function(
                    "balanceOf",
                    Arrays.asList(new Address(accountAddress)),
                    Collections.singletonList(new TypeReference<Uint256>() {})
            );
            String encodedFunction = FunctionEncoder.encode(function);

            EthCall response = web3j.ethCall(
                    Transaction.createEthCallTransaction(accountAddress, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST
            ).send();

            List<Type> output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            if (output.isEmpty()) {
                return BigInteger.ZERO;
            }
            return (BigInteger) output.get(0).getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
