package com.shuai.bitcoin.mastering.deprecated;
import com.shuai.bitcoin.mastering.WalletExample;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.BitcoinNetwork;
import org.bitcoinj.base.Coin;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

// 用peer的方式构建钱包的utxo太痛苦了，应该介入
@Deprecated
public class HdWalletExample {

    private static final int NUM_DERIVED_ADDRESSES = 10;
//    private static final String WALLET_FILE_NAME = "test.wallet";
    private static final String WALLET_FILE_NAME = "test2.wallet";
    private static final String BLOCKSTORE_FILE_NAME = "WalletTemplate-test.spvchain";

    public static void main(String[] args) {

//        NetworkParameters params = TestNet3Params.get();
        BitcoinNetwork network = BitcoinNetwork.TESTNET;
        NetworkParameters params = NetworkParameters.of(network);
        Wallet wallet = null;
        BlockStore blockStore = null;
        BlockChain chain = null;
        PeerGroup peerGroup = null;

        try {
            // 1. 设置文件路径
            File walletFile = new File(WALLET_FILE_NAME);
            File blockStoreFile = new File(BLOCKSTORE_FILE_NAME);

            // 2. 创建或加载钱包
            if (walletFile.exists()) {
                System.out.println("Loading existing wallet from: " + walletFile.getAbsolutePath());
                wallet = Wallet.loadFromFile(walletFile);
            }
            else {
                WalletExample walletExample = new WalletExample();
                wallet = walletExample.genWalletWithEntropyAndPassphrase("63748a1c1804b6c4acd170d48fae05f8",null);
//                wallet.setCreationTimeSeconds(seed.getCreationTimeSeconds()); // 设置钱包创建时间，用于SPV同步优化

                System.out.println("New HD Wallet Mnemonic Phrase (IMPORTANT! Store safely):");

                // 确保钱包有至少一个 HD 帐户（通常freshReceiveKey会自动创建）
//                wallet.freshReceiveKey();
                // 打印并派生指定数量的地址
                System.out.println("\nDeriving and watching " + NUM_DERIVED_ADDRESSES + " addresses:");
                for (int i = 0; i < NUM_DERIVED_ADDRESSES; i++) {
                    // freshReceiveKey() 会生成并返回一个新的接收地址，并自动添加到钱包的keychain中
                    // 这确保这些地址会被Wallet自动监控
//                    wallet.getIssuedReceiveAddresses();
                    Address derivedAddress = wallet.freshReceiveAddress();
                    System.out.println("Derived Address " + i  + ": " + derivedAddress);
                }
            }

//            wallet.setUTXOProvider();
            // 3. 设置区块存储
            System.out.println("Setting up BlockStore at: " + blockStoreFile.getAbsolutePath());
            blockStore = new SPVBlockStore(params, blockStoreFile);

            // 4. 设置区块链
            chain = new BlockChain(params, wallet, blockStore);

            // 5. 设置对等组
            peerGroup = new PeerGroup(params, chain);
            peerGroup.addPeerDiscovery(new DnsDiscovery(params)); // 使用DNS发现节点
            peerGroup.addWallet(wallet); // 将钱包添加到PeerGroup，以便PeerGroup同步相关交易

            // 可选：添加监听器以便在接收到比特币时得到通知
            wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
                @Override
                public void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                    System.out.println("\n--- Coins Received! ---");
                    System.out.println("Transaction ID: " + tx.getTxId());
                    System.out.println("Amount received: " + tx.getValueSentToMe(w).toFriendlyString());
                    System.out.println("New Balance: " + newBalance.toFriendlyString());
                    System.out.println("-----------------------\n");
                }
            });


            // 6. 启动同步
            System.out.println("Starting PeerGroup and syncing blockchain...");
            peerGroup.startAsync();
            peerGroup.downloadBlockChain();
//            peerGroup.awaitRunning(); // 等待PeerGroup启动

            // 等待钱包完全同步。这可能需要一些时间，因为它会下载所有区块头并过滤交易
            // 对于全新的钱包，它会从钱包创建时间（或区块0）开始同步
            System.out.println("Waiting for wallet to sync all relevant transactions...");

//            wallet.wait(); // 阻塞直到所有已知交易被处理

            System.out.println("\n--- Syncing Complete ---");
            System.out.println("Total Wallet Balance (Aggregated): " + wallet.getBalance().toFriendlyString());
            System.out.println("------------------------------------");

            // 7. 获取每个派生地址的余额 (需要迭代钱包的所有 UTXO)
            System.out.println("\nBalances for individual derived addresses:");
//            wallet.getIssuedReceiveAddresses()
            List<Address> allWatchedAddresses = wallet.getWatchedAddresses(); // 获取钱包监控的所有地址
            System.out.println("wallet balance "+wallet.getBalance());
//            for (Address address : allWatchedAddresses) {
//                Coin addressBalance = Coin.ZERO;
//                for (TransactionOutput utxo :wallet.calculateAllSpendCandidates()) {
//                    if (utxo.getScriptPubKey().isSentToAddress()) {
//                        Address outputAddress = utxo.getScriptPubKey().getToAddress(params);
//                        if (outputAddress != null && outputAddress.equals(address)) {
//                            addressBalance = addressBalance.add(utxo.getValue());
//                        }
//                    }
//                }
//                System.out.println("Address: " + address + " - Balance: " + addressBalance.toFriendlyString());
//            }


            // 保持程序运行，直到用户输入（可选，以便观察）
//            System.out.println("\nPress Enter to stop the app...");
//            System.in.read();

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 8. 确保在程序结束时正确关闭资源
            if (peerGroup != null) {
                System.out.println("Stopping PeerGroup...");
                peerGroup.stopAsync();
//                peerGroup.(5, TimeUnit.SECONDS); // 等待关闭，超时5秒
            }
            if (blockStore != null) {
                try {
                    blockStore.close();
                    System.out.println("BlockStore closed.");
                } catch (Exception e) {
                    System.err.println("Error closing BlockStore: " + e.getMessage());
                }
            }
            if (wallet != null) {
                try {
                    wallet.saveToFile(new File(WALLET_FILE_NAME)); // 保存钱包
                    System.out.println("Wallet saved.");
                } catch (IOException e) {
                    System.err.println("Error saving wallet: " + e.getMessage());
                }
            }
            System.out.println("Application terminated.");
        }
    }
}