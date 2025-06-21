package com.shuai.wallet;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams; // 或 TestNet3Params for testnet
import org.bitcoinj.params.Networks;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class EthHDWalletManager {

    private static final Network network = BitcoinNetwork.MAINNET;

    /**
     * 生成助记词 (12个英文单词)
     */
    public static List<String> generateMnemonic() throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[16]; // 128 bits = 12 words
        secureRandom.nextBytes(entropy);
        return MnemonicCode.INSTANCE.toMnemonic(entropy);
    }

    /**
     * 从助记词生成主种子
     */
    public static DeterministicSeed createMasterSeed(List<String> mnemonic, String passphrase) {
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, passphrase, creationTimeSeconds);
        return seed;
    }

    /**
     * 离线派生用户充值地址的私钥 (用于归集/提现签名)
     * 路径: m/44'/60'/0'/0/{userId}/0
     */
    public static String  deriveUserKeyPair(DeterministicSeed masterSeed, int userId) {
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

        String publicKeyHex = Numeric.toHexStringNoPrefix(key.getPubKey());
        String address = Keys.toChecksumAddress(Keys.getAddress(publicKeyHex)); // 确保使用 checksum address

//        String address = Numeric.prependHexPrefix(Keys.getAddress(key.getPublicKeyAsHex()));
        return address ;
    }

    /**
     * 离线生成账户xpub (m/44'/60'/0'/0)
     */
    public static String getAccountXpub(DeterministicSeed masterSeed) {
        DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(masterSeed).build();
        List<ChildNumber> path = Arrays.asList(
            new ChildNumber(44, true),
            new ChildNumber(60, true),
            new ChildNumber(0, true),
            new ChildNumber(0, false)
        );
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(masterSeed.getSeedBytes());
        log.info("Master Private Key (hex): {}", masterPrivateKey.getPrivateKeyAsHex());

        DeterministicKey key = chain.getKeyByPath(path, true);
        System.out.println("xpub路径: " + key.getPathAsString());
        return key.serializePubB58(network);
    }

    /**
     * 在线派生用户充值地址 (只用xpub，不涉及私钥)
     * 路径: {userId}/0
     * 这个方法目前调试不通，可能是因为DeterministicKey.deserializeB58(accountXpub, network); 返回的HDpath没有丢失数据了
     * 直接用
     */
    public static String deriveUserAddressFromXpub(String accountXpub, int userId) {
        DeterministicKey xpubKey = DeterministicKey.deserializeB58(accountXpub, network);
        System.out.println("xpubKey路径: " + xpubKey.getPathAsString());
        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(xpubKey, new ChildNumber(userId, false));
        String address = Numeric.prependHexPrefix(Keys.getAddress(childKey.getPublicKeyAsHex()));
        return address;
    }

    public static void main(String[] args) throws Exception {
        // 1. 离线生成助记词
        List<String> mnemonic = generateMnemonic();
        mnemonic = Lists.list("amused","multiply","ridge","ugly","volume","leisure","surge","fun","pony","clutch","surprise","whisper");
//        hybrid risk elite mountain logic crazy sword cable series evolve idea donor
//        mnemonic = Lists.list("hybrid","risk","elite","mountain","logic","crazy","sword","cable","series","evolve","idea","donor");
        System.out.println("生成的助记词: " + String.join(" ", mnemonic));

        // 2. 离线生成主种子
        String passphrase = ""; // 可选密码，生产环境建议设置
        DeterministicSeed masterSeed = createMasterSeed(mnemonic, passphrase);
        System.out.println("主种子: " + masterSeed.toHexString());

        // 3. 离线生成账户xpub (m/44'/60'/0'/0)
        String accountXpub = getAccountXpub(masterSeed);
        System.out.println("账户xpub (m/44'/60'/0'/0): " + accountXpub);

        // 4. 在线为不同用户派生充值地址
        int userId1 = 1;
//        int userId2 = 1;
        String address1 = deriveUserAddressFromXpub(accountXpub, userId1);
        System.out.println("用户 " + userId1 + " 的充值地址: " + address1);
        String offlineAddress1 = deriveUserKeyPair(masterSeed, userId1);
        System.out.println("用户 " + userId1 + " 离线派生私钥对应地址: " + offlineAddress1);
        System.out.println("地址是否一致: " + address1.equalsIgnoreCase(offlineAddress1));

        for (int i = 0;i<10;i++) {
            String address = deriveUserKeyPair(masterSeed, i);
            System.out.println("address"+address);

        }
    }
}