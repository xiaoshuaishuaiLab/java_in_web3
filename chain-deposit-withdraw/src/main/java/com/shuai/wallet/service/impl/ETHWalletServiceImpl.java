package com.shuai.wallet.service.impl;

import com.shuai.wallet.service.WalletService;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

@Service
public class ETHWalletServiceImpl implements WalletService {

    private String mnemonics = "amused multiply ridge ugly volume leisure surge fun pony clutch surprise whisper";
    private String masterSeed = "a31a5d8158ed3dd261996c5ef94b5d2c11f0e641ed3e7a7e3fcd2c55a56acbb974ca50ef49dd10ae6598d8dee6a67900a200f99a92d65f09e9d7d5dcea49e3b4";
    // 账户xpub (m/44'/60'/0'/0)
    private String masterXPUB = "xpub6Dr94hHJ9cGuNXHeT76bL2gjab4w1brFnKrvbK5hH8XQV7vNAkFJU2EUv4vXyijbR7NszhZXD1PzxZ8Y4QTqrMxaeyAabwuhVCiHGaDUm3j";
    private static final Network network = BitcoinNetwork.MAINNET;


    @Override
    public String getAddress(Integer childNum) {
        DeterministicKey xpubKey = DeterministicKey.deserializeB58(masterXPUB, network);
        System.out.println("xpubKey路径: " + xpubKey.getPathAsString());
        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(xpubKey, new ChildNumber(childNum, false));
        String address = Numeric.prependHexPrefix(Keys.getAddress(childKey.getPublicKeyAsHex()));
        return address;
    }
}
