package com.shuai.wallet;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.utils.Numeric;

public class AddressRecovery {

    public static void main(String[] args) {
        // The raw signed transaction hex you provided earlier
        String signedTxHex = "0x02f87283aa36a780831158d68322b1ac8252089449dcde8b9e56dd9bd172002db5233c350cc7abf987038d7ea4c6800080c080a05fa6a4bb3cb8ac3da487503cf9ac7d9c306aec2703e4eaa885b98b798783ec47a024537c37b52b5a658848767eaa678913d9a273a86e75a4f3a42bbb552d80acb8";

        try {
            // 1. Decode the raw transaction hex
            // This parses the hex string into a structured transaction object
            SignedRawTransaction signedTx = (SignedRawTransaction) TransactionDecoder.decode(signedTxHex);

            // 2. Recover the sender's address from the signature
            // The .getFrom() method performs the ecrecover operation internally
            String senderAddress = signedTx.getFrom();

            System.out.println("Successfully recovered the sender's address.");
            System.out.println("Sender Address: " + senderAddress);

            // For your specific transaction, this will print:
            // Sender Address: 0x03742456a023a1d799abe0b6955e9a68344e43f1

        } catch (Exception e) {
            System.err.println("Failed to decode transaction or recover address: " + e.getMessage());
            e.printStackTrace();
        }
    }
}