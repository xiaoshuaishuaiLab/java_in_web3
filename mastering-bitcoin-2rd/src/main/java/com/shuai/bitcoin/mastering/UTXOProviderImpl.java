package com.shuai.bitcoin.mastering;

import com.google.common.collect.Lists;
import com.shuai.bitcoin.mastering.utils.HttpUtil;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.Network;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.base.internal.ByteUtils;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.UTXOProvider;
import org.bitcoinj.core.UTXOProviderException;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.script.Script;

import java.util.List;

public class UTXOProviderImpl implements UTXOProvider {

    // todo 改成动态化的
    @Override
    public List<UTXO> getOpenTransactionOutputs(List<ECKey> keys) throws UTXOProviderException {
        Sha256Hash hash = Sha256Hash.wrap("ce476237b9d5e60e31dd82fa1dcfc8c6394cac6c8125b779453309b4b8a27203");
        Script script = Script.parse(ByteUtils.parseHex("76a914db63afe4fab322a4cb1dd7a9aadb9ec29f52479388ac"));
        Coin value = Coin.valueOf(181152);
        int height = 4419113;
        boolean coinbase = false;
        String address = "n1WyhSmnSYc94YHkCvWuWtyYLX9khmt9ye";
        UTXO utxo = new UTXO(hash, 0, value, height, coinbase, script, address);
        return Lists.newArrayList(utxo);
    }

    @Override
    public int getChainHeadHeight() throws UTXOProviderException {
        return Integer.parseInt(HttpUtil.get("https://mempool.space/testnet/api/blocks/tip/height"));
//        return 0;
    }


    @Override
    public Network network() {
        return BitcoinNetwork.TESTNET;
    }
}
