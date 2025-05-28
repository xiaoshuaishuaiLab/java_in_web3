package com.shuai.bitcoin.mastering.config;

import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Network;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class BitcoinConfiguration {

    /**
     * 第一次启动的时候，会下载区块头，耗时比较久
     * org.bitcoinj.core.Peer#processHeaders(org.bitcoinj.core.HeadersMessage)
     * org.bitcoinj.core.HeadersMessage
     * @return
     * @throws Exception
     */
    @Bean
    public PeerGroup mainnetPeerGroup() throws Exception {
        final Network network = BitcoinNetwork.MAINNET;
        final NetworkParameters params = NetworkParameters.of(network);

        File blockStoreFile = new File("spvblockstore");
        BlockStore blockStore = new SPVBlockStore(params, blockStoreFile);

        BlockChain chain = new BlockChain(network, blockStore);

        PeerGroup peerGroup = new PeerGroup(network, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.start();
        peerGroup.downloadBlockChain();
//        peerGroup.waitForPeers(1).get();
//        Peer peer = peerGroup.getConnectedPeers().get(0);
        return peerGroup;
    }

    @Bean
    public PeerGroup testnetPeerGroup() throws Exception {
        final Network network = BitcoinNetwork.TESTNET;
        final NetworkParameters params = NetworkParameters.of(network);

        File blockStoreFile = new File("spvblockstore.testnet");
        BlockStore blockStore = new SPVBlockStore(params, blockStoreFile);

        BlockChain chain = new BlockChain(network, blockStore);

        PeerGroup peerGroup = new PeerGroup(network, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.start();
        peerGroup.downloadBlockChain();
//        peerGroup.waitForPeers(1).get();
//        Peer peer = peerGroup.getConnectedPeers().get(0);
        return peerGroup;
    }
}
