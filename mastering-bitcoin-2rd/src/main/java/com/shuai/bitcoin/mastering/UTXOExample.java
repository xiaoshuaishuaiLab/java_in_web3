//package com.shuai.bitcoin.mastering;
//
//import jakarta.annotation.Resource;
//import org.bitcoinj.core.PeerGroup;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UTXOExample {
//
//    @Resource(name = "testnetPeerGroup")
//    private PeerGroup peerGroup;
//
//    public void getUTXO() {
//        peerGroup.block.getOpenTransactionOutputs(address).forEach(utxo -> {
//            utxos.add(utxo);
//            System.out.println("Found UTXO: " + utxo.getHash() + ":" + utxo.getIndex() +
//                    " Amount: " + utxo.getValue().toFriendlyString());
//        });
//    }
//
//}
