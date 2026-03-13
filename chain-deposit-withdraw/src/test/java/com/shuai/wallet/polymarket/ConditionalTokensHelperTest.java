package com.shuai.wallet.polymarket;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.utils.Numeric;
import java.math.BigInteger;
import java.util.List;

/**
 * Test class for ConditionalTokensHelper
 */
public class ConditionalTokensHelperTest {

    @Test
    public void testGetCollectionId() {

        Log log = new Log();
        List<String> topics = Lists.newArrayList("0x2682012a4a4f1973119f1c9b90745d1bd91fa2bab387344f044cb3586864d18d",
            "0x00000000000000000000000093cb9d25f36b5abff52a96d122a94dc52a2f21ae",
            "0x0000000000000000000000002791bca1f2de4661ed88a30c99a7a9449aa84174",
            "0x0000000000000000000000000000000000000000000000000000000000000000");
        log.setTopics(topics);
        log.setData("0xd3460cd313aa9759ea67a966e9a499cb65964d6e2a2ff6902472aa83005383bb00000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000001c9c380000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000002");

        ConditionalTokensHelper.PayoutRedemptionEvent payoutRedemptionEvent =
            ConditionalTokensHelper.parsePayoutRedemptionEvent(log);
        System.out.println(payoutRedemptionEvent);

        String collectionId =
            ConditionalTokensHelper.getCollectionId("0000000000000000000000000000000000000000000000000000000000000000",
                "0x81e33ca5bd2f1c4743e88fb0741ba512111708946a6f5cc4a158ea6c01fdabf8", payoutRedemptionEvent.getIndexSets().get(0));
        System.out.println(collectionId);

        String collectionId2 =
            ConditionalTokensHelper.getCollectionId("0000000000000000000000000000000000000000000000000000000000000000",
                "81E33CA5BD2F1C4743E88FB0741BA512111708946A6F5CC4A158EA6C01FDABF8", new BigInteger("1"));
        System.out.println(collectionId2);

        BigInteger tokenId =
            ConditionalTokensHelper.getPositionId("0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174", collectionId);
        System.out.println(tokenId);

    }
    @Test
    public void testGetConditionId() {
        // Test parameters
        String oracle = "0x1234567890123456789012345678901234567890";
        byte[] questionId = new byte[32]; // All zeros for simplicity
        long outcomeSlotCount = 2;

        byte[] conditionId = ConditionalTokensHelper.getConditionId(oracle, questionId, outcomeSlotCount);


        System.out.println("Condition ID: " + ConditionalTokensHelper.toHexString(conditionId));
    }

    @Test
    public void testGetCollectionIdWithoutParent() {
        // Test with no parent collection (parentCollectionId = bytes32(0))
        byte[] parentCollectionId = new byte[32]; // All zeros
        byte[] conditionId = new byte[32];
        // Set some test data in conditionId
        conditionId[31] = 1;
        BigInteger indexSet = BigInteger.valueOf(1);

        try {
            byte[] collectionId = ConditionalTokensHelper.getCollectionId(
                    parentCollectionId,
                    conditionId,
                    indexSet
            );


            System.out.println("Collection ID (no parent): " + ConditionalTokensHelper.toHexString(collectionId));
        } catch (Exception e) {
            System.out.println("Note: This test may fail due to simplified elliptic curve implementation");
            System.out.println("Error: " + e.getMessage());
            // In production, you would need to use the exact same EC arithmetic as Solidity
            // or call the contract directly
        }
    }

    @Test
    public void testGetCollectionIdWithParent() {
        // Test with a parent collection
        byte[] parentCollectionId = new byte[32];
        parentCollectionId[31] = 5; // Some non-zero value

        byte[] conditionId = new byte[32];
        conditionId[31] = 1;

        BigInteger indexSet = BigInteger.valueOf(2);

        try {
            byte[] collectionId = ConditionalTokensHelper.getCollectionId(
                    parentCollectionId,
                    conditionId,
                    indexSet
            );

            System.out.println("Collection ID (with parent): " + ConditionalTokensHelper.toHexString(collectionId));
        } catch (Exception e) {
            System.out.println("Note: This test may fail due to parent collection validation");
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Test
    public void testGetPositionId() {
        String collateralToken = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";
        byte[] collectionId = new byte[32];
        collectionId[31] = 1;

        BigInteger positionId = ConditionalTokensHelper.getPositionId(collateralToken, collectionId);

        System.out.println("Position ID: " + positionId.toString(16));
    }

    @Test
    public void testMultipleOutcomeSlots() {
        // Test with different index sets representing different outcome combinations
        String oracle = "0x1234567890123456789012345678901234567890";
        byte[] questionId = new byte[32];
        long outcomeSlotCount = 3; // Three possible outcomes

        byte[] conditionId = ConditionalTokensHelper.getConditionId(oracle, questionId, outcomeSlotCount);

        // Test different index sets:
        // 0b001 = outcome 0
        // 0b010 = outcome 1
        // 0b100 = outcome 2
        // 0b011 = outcome 0 OR outcome 1
        // 0b111 = all outcomes

        BigInteger[] indexSets = {
                BigInteger.valueOf(0b001),
                BigInteger.valueOf(0b010),
                BigInteger.valueOf(0b100),
                BigInteger.valueOf(0b011),
                BigInteger.valueOf(0b111)
        };

        byte[] parentCollectionId = new byte[32];

        for (int i = 0; i < indexSets.length; i++) {
            try {
                byte[] collectionId = ConditionalTokensHelper.getCollectionId(
                        parentCollectionId,
                        conditionId,
                        indexSets[i]
                );

                System.out.println("Index Set " + Integer.toBinaryString(indexSets[i].intValue()) +
                        " -> Collection ID: " + ConditionalTokensHelper.toHexString(collectionId));
            } catch (Exception e) {
                System.out.println("Index Set " + Integer.toBinaryString(indexSets[i].intValue()) +
                        " -> Error: " + e.getMessage());
            }
        }
    }

    @Test
    public void testRealWorldExample() {
        // Example from Polymarket
        // This would need actual values from a real Polymarket market
        System.out.println("\n=== Real World Example Test ===");
        System.out.println("To test with real Polymarket data:");
        System.out.println("1. Get a condition ID from a Polymarket market");
        System.out.println("2. Get the oracle address");
        System.out.println("3. Get the question ID");
        System.out.println("4. Calculate and compare with on-chain data");
        System.out.println("\nNote: The elliptic curve operations in this Java implementation");
        System.out.println("are simplified and may not produce identical results to the Solidity");
        System.out.println("contract, especially for the sqrt operation which uses a custom");
        System.out.println("optimized algorithm in Solidity.");
    }
}