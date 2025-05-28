package com.shuai.bitcoin.mastering;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.base.internal.ByteUtils;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WalletExample {


    public Wallet genWalletWithEntropyAndPassphrase(String entropyHex, String passphrase) {

        // 1. 生成种子
        DeterministicSeed seed = genSeedWithEntropyAndPassphrase(entropyHex, passphrase);

        // 2. 指定账户路径：这里指定 m/44H/1H/0H 作为账户前缀
        List<ChildNumber> accountPath = HDPath.parsePath("m/44H/1H/0H");

// 3. 根据种子和账户路径构建 DeterministicKeyChain
        DeterministicKeyChain keyChain = DeterministicKeyChain.builder()
                .seed(seed)
                .accountPath(accountPath)
                .build();

// 4. 构造 KeyChainGroup 并使用该 KeyChain
        KeyChainGroup keyChainGroup = KeyChainGroup.builder(BitcoinNetwork.TESTNET).addChain(keyChain)
                .build();

// 5. 使用 KeyChainGroup 构造 Wallet
        Wallet wallet = new Wallet(BitcoinNetwork.TESTNET, keyChainGroup);


//        DeterministicSeed seed = genSeedWithEntropyAndPassphrase("63748a1c1804b6c4acd170d48fae05f8", null);
//        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
//        Wallet wallet = Wallet.fromMasterKey(BitcoinNetwork.TESTNET,masterPrivateKey, ScriptType.P2PKH, ChildNumber.PURPOSE_BIP44);
        // 获取一个新的（未使用的）子账号地址
        Address freshAddress = wallet.freshReceiveAddress();
        System.out.println("freshReceiveAddress: " + freshAddress);

        return wallet;
//        wallet.saveToFile();

        // 获取钱包当前分支下所有已使用的接收地址
//        List<DeterministicKey> issuedAddresses = wallet.getActiveKeyChain().getIssuedReceiveKeys();
//        System.out.println(issuedAddresses);



//        List<ECKey> importedKeys = wallet.getImportedKeys();
//        System.out.println(importedKeys);
    }

    public DeterministicSeed genSeedWithEntropyAndPassphrase(String entropyHex, String passphrase) {
        log.info("entropyHex = {},passphrase = {}", entropyHex, passphrase);
        byte[] entropy = ByteUtils.parseHex(entropyHex);

        // 生成助记词
        List<String> mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy);
        log.info("mnemonic={} ", String.join(" ", mnemonic));

        // 将助记词转换为种子
        if (passphrase == null) {
            passphrase = "";
        }
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(mnemonic, passphrase);
        log.info("seed={} ", seed.toHexString());
        return seed;
    }


    public DeterministicSeed genSeed() {
        // 生成助记词
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[16]; // 16字节生成12个单词，32字节生成24个单词
        secureRandom.nextBytes(entropy);
        log.info("random = {}", ByteUtils.formatHex(entropy));
        List<String> mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy);
        log.info("mnemonic: " + String.join(" ", mnemonic));
        // 将助记词转换为种子
        DeterministicSeed seed = DeterministicSeed.ofMnemonic(mnemonic, "");
        log.info("seed: " + seed.toHexString());
        return seed;
    }


    //    https://iancoleman.io/bip39/#chinese_simplified 可以在此网站进行验证
    public List<DeterministicKey> genHDWalletAddressWithSeed(DeterministicSeed seed, int childNum, BitcoinNetwork bitcoinNetwork) {
        List<DeterministicKey> result = Lists.newArrayList();

        // 1. 生成种子
        byte[] seedBytes = seed.getSeedBytes();
        log.info("Seed (hex): {}", seed.toHexString());

        // 2. 用种子生成HD钱包的主密钥（BIP32 Root Key）
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);
        result.add(masterPrivateKey);
        log.info("Master Private Key (hex): {}", masterPrivateKey.getPrivateKeyAsHex());
        log.info("Master Public Key (hex): {}", masterPrivateKey.getPublicKeyAsHex());
        log.info("Master Chain Code: {}", ByteUtils.formatHex(masterPrivateKey.getChainCode()));
        // 打印扩展私钥和公钥
        log.info("Master Extended Private Key: {}", masterPrivateKey.serializePrivB58(bitcoinNetwork));
        log.info("Master Extended Public Key: {}", masterPrivateKey.serializePubB58(bitcoinNetwork));

        //  3. 创建 HD 钱包链
        DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
        // 4. BIP44 路径: m/44'/0'/0'/0/i
        // 对于测试网使用 m/44'/1'/0'/0/i，主网使用 m/44'/0'/0'/0/i
        String path = bitcoinNetwork == BitcoinNetwork.MAINNET ?
                "m/44H/0H/0H/0" : // 主网
                "m/44H/1H/0H/0";  // 测试网;
        List<ChildNumber> bip44path = HDPath.parsePath(path);
        // 5. 派生前缀节点
        DeterministicKey parentKey = keyChain.getKeyByPath(bip44path, true);

        // 6. 生成10个地址
        for (int i = 0; i < childNum; i++) {
            DeterministicKey childKey = HDKeyDerivation.deriveChildKey(parentKey, i);
            result.add(childKey);
            ECKey ecKey = ECKey.fromPrivate(childKey.getPrivKey());
            Address address = ecKey.toAddress(ScriptType.P2PKH, bitcoinNetwork);
            log.info("address = {},Private Key (wif) = {},Public Key (hex)= {},", address, ecKey.getPrivateKeyAsWiF(bitcoinNetwork), ecKey.getPublicKeyAsHex());
        }
        return result;
    }

    @Test
    public void testHDAddress() {
        BitcoinNetwork network = BitcoinNetwork.MAINNET;
        DeterministicSeed seed = genSeedWithEntropyAndPassphrase("0c1e24e5917779d297e14d45f14e1a1a", null);
        List<DeterministicKey> keys = genHDWalletAddressWithSeed(seed, 2, network);
        DeterministicKey firstChild = keys.get(1);

        assert firstChild.getPublicKeyAsHex().equals("036740c4f55d64fb6c9bc412084638b80062cee07f6c84b205671584e82a7c96b7");
        assert firstChild.getPrivateKeyAsWiF(network).equals("L27rKoVgW4dYc1BxhD1X7jp9ixPGwnn3E2eggznmpYcuktDYPKaH");
        Address firstChildAddress = ECKey.fromPrivate(firstChild.getPrivKey()).toAddress(ScriptType.P2PKH, network);
        // 此处toString() 对应 org.bitcoinj.base.LegacyAddress.toString
        assert firstChildAddress.toString().equals("1HQ3rb7nyLPrjnuW85MUknPekwkn7poAUm");
        // 更多对照，可以参考 https://iancoleman.io/bip39/#chinese_simplified
    }


    // 用书中的输入，生成相应的助记词&种子
    @Test
    public void testGenSeedWithFixEntropy() {
        List<String> mnemonicList1 = Lists.newArrayList("army", "van", "defense", "carry", "jealous", "true", "garbage", "claim", "echo", "media", "make", "crunch");
        DeterministicSeed seed = genSeedWithEntropyAndPassphrase("0c1e24e5917779d297e14d45f14e1a1a", null);
        assert seed.toHexString().equals("5b56c417303faa3fcba7e57400e120a0ca83ec5a4fc9ffba757fbe63fbd77a89a1a3be4c67196f57c39a88b76373733891bfaba16ed27a813ceed498804c0570");
        assert seed.getMnemonicCode().equals(mnemonicList1);

        seed = genSeedWithEntropyAndPassphrase("0c1e24e5917779d297e14d45f14e1a1a", "SuperDuperSecret");
        assert seed.toHexString().equals("3b5df16df2157104cfdd22830162a5e170c0161653e3afe6c88defeefb0818c793dbb28ab3ab091897d0715861dc8a18358f80b79d49acf64142ae57037d1d54");
        assert seed.getMnemonicCode().equals(mnemonicList1);

        List<String> mnemonicList2 = Lists.newArrayList("cake", "apple", "borrow", "silk", "endorse", "fitness", "top"
                , "denial", "coil", "riot", "stay", "wolf", "luggage", "oxygen", "faint", "major", "edit", "measure", "invite", "love", "trap", "field", "dilemma", "oblige");
        seed = genSeedWithEntropyAndPassphrase("2041546864449caff939d32d574753fe684d3c947c3346713dd8423e74abcf8c", null);
        assert seed.toHexString().equals("3269bce2674acbd188d4f120072b13b088a0ecf87c6e4cae41657a0bb78f5315b33b3a04356e53d062e55f1e0deaa082df8d487381379df848a6ad7e98798404");
        assert seed.getMnemonicCode().equals(mnemonicList2);
    }

    @Test
    public void testGenSeed() {
        genSeed();
    }


}
