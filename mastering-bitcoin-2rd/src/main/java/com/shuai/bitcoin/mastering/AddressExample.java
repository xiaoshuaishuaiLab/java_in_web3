package com.shuai.bitcoin.mastering;

import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

public class AddressExample {

    @Test
    public void testAddressExample() {
        // 选择主网参数
        final Network network = BitcoinNetwork.MAINNET;
        final NetworkParameters params = NetworkParameters.of(network);

        // 生成随机私钥
        ECKey key = new ECKey();

        // 私钥（16进制）
        String privKeyHex = key.getPrivateKeyAsHex();

        System.out.println("Private Key (hex) is: " + privKeyHex);

        // 私钥（十进制）
        System.out.println("Private Key (decimal) is: " + key.getPrivKey());

        // WIF 格式私钥
//        String wif = key.getPrivateKeyAsWiF(params);
        String wif = key.getPrivateKeyEncoded(network).toString();

        // 压缩私钥的 WIF 格式（bitcoinj 默认生成的就是压缩格式）
        System.out.println("Private Key (WIF-Compressed) is: " + wif);

        // 公钥（椭圆曲线点坐标）
        System.out.println("Public Key (x,y) coordinates is: (" +
                key.getPubKeyPoint().getXCoord().toBigInteger() + ", " +
                key.getPubKeyPoint().getYCoord().toBigInteger() + ")");

        // 公钥（hex，压缩格式）
        String pubKeyHex = key.getPublicKeyAsHex();
        System.out.println("Compressed Public Key (hex) is: " + pubKeyHex);

        // 公钥（hex，非压缩格式）
        String pubKeyHexUncompressed = key.decompress().getPublicKeyAsHex();
        System.out.println("Uncompressed Public Key (hex) is: " + pubKeyHexUncompressed);

        // 比特币地址（压缩公钥）
        Address address = key.toAddress(ScriptType.P2PKH, network);
        System.out.println("Bitcoin Address (b58check) is: " + address);

        // 比特币地址（非压缩公钥）
        Address addressUncompressed = key.decompress().toAddress(ScriptType.P2PKH, network);
        System.out.println("Uncompressed Bitcoin Address (b58check) is: " + addressUncompressed);
    }

    @Test
    public void testGenPubKeyHexFromprivKeyHex() {
        // 私钥（16进制字符串）
        String privKeyHex = "038109007313a5807b2eccc082c8c3fbb988a973cacf1a7df9ce725c31b14776";
        // 将私钥转换为字节数组
        byte[] privKeyBytes = Hex.decode(privKeyHex);
        // 用私钥字节数组创建 ECKey 对象
        ECKey key = ECKey.fromPrivate(privKeyBytes);

        // 获取公钥（压缩格式）
        String pubKeyHex = key.getPublicKeyAsHex();
        System.out.println("公钥（压缩）: " + pubKeyHex);
    }


}
