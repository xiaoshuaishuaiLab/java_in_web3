package com.shuai.wallet.polymarket;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Helper class for ConditionalTokens operations
 * Port of ConditionalTokens.sol CTHelpers library to Java
 */
public class ConditionalTokensHelper {

    // BN128 curve parameters
    // Prime field modulus
    private static final BigInteger P = new BigInteger("21888242871839275222246405745257275088696311157297823662689037894645226208583");
    // Curve parameter B (y^2 = x^3 + B)
    private static final BigInteger B = BigInteger.valueOf(3);

    /**
     * Constructs a condition ID from an oracle, a question ID, and the outcome slot count
     *
     * @param oracle The account assigned to report the result for the prepared condition
     * @param questionId An identifier for the question to be answered by the oracle
     * @param outcomeSlotCount The number of outcome slots which should be used for this condition
     * @return The condition ID
     */
    public static byte[] getConditionId(String oracle, byte[] questionId, long outcomeSlotCount) {
        // Remove "0x" prefix if present
        String oracleAddress = Numeric.cleanHexPrefix(oracle);

        // Build the packed encoding: oracle (20 bytes) + questionId (32 bytes) + outcomeSlotCount (32 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(20 + 32 + 32);
        buffer.put(Numeric.hexStringToByteArray(oracleAddress));
        buffer.put(questionId);
        buffer.put(toBigEndianBytes(BigInteger.valueOf(outcomeSlotCount), 32));

        return Hash.sha3(buffer.array());
    }

    /**
     * Constructs an outcome collection ID from a parent collection and an outcome collection
     *
     * @param parentCollectionId Collection ID of the parent outcome collection, or null/zeros if there's no parent
     * @param conditionId Condition ID of the outcome collection to combine with the parent outcome collection
     * @param indexSet Index set of the outcome collection to combine with the parent outcome collection
     * @return The collection ID
     */
    public static byte[] getCollectionId(byte[] parentCollectionId, byte[] conditionId, BigInteger indexSet) {
        // Step 1: Compute x1 = keccak256(conditionId, indexSet)
        ByteBuffer buffer = ByteBuffer.allocate(32 + 32);
        buffer.put(conditionId);
        buffer.put(toBigEndianBytes(indexSet, 32));

        byte[] hash = Hash.sha3(buffer.array());
        BigInteger x1 = new BigInteger(1, hash);

        // Check if the highest bit is set (odd flag for y-coordinate)
        boolean odd = x1.testBit(255);

        // Step 2: Find a point on the curve
        BigInteger y1;
        BigInteger yy;
        do {
            x1 = x1.add(BigInteger.ONE).mod(P);
            // yy = x1^3 + B (mod P)
            yy = x1.modPow(BigInteger.valueOf(3), P).add(B).mod(P);
            y1 = modSqrt(yy, P);
        } while (y1 == null || !y1.modPow(BigInteger.TWO, P).equals(yy));

        // Adjust y1 based on odd flag
        if ((odd && !y1.testBit(0)) || (!odd && y1.testBit(0))) {
            y1 = P.subtract(y1);
        }

        // Step 3: Process parent collection if present
        BigInteger x2 = new BigInteger(1, parentCollectionId != null ? parentCollectionId : new byte[32]);

        if (!x2.equals(BigInteger.ZERO)) {
            // Extract the odd flag from bit 254
            boolean odd2 = x2.testBit(254);

            // Clear the top 2 bits: (x2 << 2) >> 2
            x2 = x2.clearBit(255).clearBit(254);

            // Compute y2 for the parent point
            yy = x2.modPow(BigInteger.valueOf(3), P).add(B).mod(P);
            BigInteger y2 = modSqrt(yy, P);

            if (y2 == null || !y2.modPow(BigInteger.TWO, P).equals(yy)) {
                throw new IllegalArgumentException("Invalid parent collection ID");
            }

            // Adjust y2 based on odd flag
            if ((odd2 && !y2.testBit(0)) || (!odd2 && y2.testBit(0))) {
                y2 = P.subtract(y2);
            }

            // Step 4: Perform elliptic curve point addition (x1, y1) + (x2, y2)
            BigInteger[] result = ecAdd(x1, y1, x2, y2);
            x1 = result[0];
            y1 = result[1];
        }

        // Step 5: Encode the result
        // If y1 is odd, set bit 254
        if (y1.testBit(0)) {
            x1 = x1.setBit(254);
        }

        return toBigEndianBytes(x1, 32);
    }

    /**
     * Constructs a position ID from a collateral token and an outcome collection
     *
     * @param collateralToken Collateral token which backs the position
     * @param collectionId ID of the outcome collection associated with this position
     * @return The position ID as BigInteger
     */
    public static BigInteger getPositionId(String collateralToken, byte[] collectionId) {
        String tokenAddress = Numeric.cleanHexPrefix(collateralToken);

        ByteBuffer buffer = ByteBuffer.allocate(20 + 32);
        buffer.put(Numeric.hexStringToByteArray(tokenAddress));
        buffer.put(collectionId);

        byte[] hash = Hash.sha3(buffer.array());
        return new BigInteger(1, hash);
    }

    /**
     * Performs elliptic curve point addition on the BN128 curve
     * This is a simplified implementation. In production, you should use a proper EC library.
     *
     * @param x1 X coordinate of first point
     * @param y1 Y coordinate of first point
     * @param x2 X coordinate of second point
     * @param y2 Y coordinate of second point
     * @return Array containing [x3, y3] coordinates of the result
     */
    private static BigInteger[] ecAdd(BigInteger x1, BigInteger y1, BigInteger x2, BigInteger y2) {
        // Handle point at infinity cases
        if (x1.equals(BigInteger.ZERO) && y1.equals(BigInteger.ZERO)) {
            return new BigInteger[]{x2, y2};
        }
        if (x2.equals(BigInteger.ZERO) && y2.equals(BigInteger.ZERO)) {
            return new BigInteger[]{x1, y1};
        }

        // If points are the same, use point doubling
        if (x1.equals(x2) && y1.equals(y2)) {
            return ecDouble(x1, y1);
        }

        // If x coordinates are the same but y are different, result is point at infinity
        if (x1.equals(x2)) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ZERO};
        }

        // Standard point addition formula
        // s = (y2 - y1) / (x2 - x1)
        BigInteger numerator = y2.subtract(y1).mod(P);
        BigInteger denominator = x2.subtract(x1).mod(P);
        BigInteger s = numerator.multiply(denominator.modInverse(P)).mod(P);

        // x3 = s^2 - x1 - x2
        BigInteger x3 = s.modPow(BigInteger.TWO, P).subtract(x1).subtract(x2).mod(P);

        // y3 = s(x1 - x3) - y1
        BigInteger y3 = s.multiply(x1.subtract(x3)).subtract(y1).mod(P);

        return new BigInteger[]{x3, y3};
    }

    /**
     * Performs elliptic curve point doubling on the BN128 curve
     *
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @return Array containing [x3, y3] coordinates of the result
     */
    private static BigInteger[] ecDouble(BigInteger x, BigInteger y) {
        if (y.equals(BigInteger.ZERO)) {
            return new BigInteger[]{BigInteger.ZERO, BigInteger.ZERO};
        }

        // s = (3 * x^2) / (2 * y)
        BigInteger numerator = BigInteger.valueOf(3).multiply(x.modPow(BigInteger.TWO, P)).mod(P);
        BigInteger denominator = BigInteger.TWO.multiply(y).mod(P);
        BigInteger s = numerator.multiply(denominator.modInverse(P)).mod(P);

        // x3 = s^2 - 2*x
        BigInteger x3 = s.modPow(BigInteger.TWO, P).subtract(x.multiply(BigInteger.TWO)).mod(P);

        // y3 = s(x - x3) - y
        BigInteger y3 = s.multiply(x.subtract(x3)).subtract(y).mod(P);

        return new BigInteger[]{x3, y3};
    }

    /**
     * Computes modular square root using Tonelli-Shanks algorithm
     * For the BN128 curve, we can use the simpler case since P ≡ 3 (mod 4)
     *
     * @param a The value to find square root of
     * @param p The modulus
     * @return The square root, or null if no square root exists
     */
    private static BigInteger modSqrt(BigInteger a, BigInteger p) {
        // For p ≡ 3 (mod 4), we can use: sqrt(a) = a^((p+1)/4) mod p
        // However, the Solidity code uses a more complex custom algorithm
        // For production use, we would need to implement the exact same algorithm
        // or use the precompiled contract approach

        // Simplified implementation (may not match Solidity exactly)
        if (a.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }

        // Check if a is a quadratic residue
        BigInteger legendreSymbol = a.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p);
        if (!legendreSymbol.equals(BigInteger.ONE)) {
            return null; // Not a quadratic residue
        }

        // Since P ≡ 3 (mod 4), we can use the simple formula
        BigInteger exponent = p.add(BigInteger.ONE).divide(BigInteger.valueOf(4));
        return a.modPow(exponent, p);
    }

    /**
     * Converts a BigInteger to a big-endian byte array of specified length
     *
     * @param value The value to convert
     * @param length The desired length in bytes
     * @return The byte array
     */
    private static byte[] toBigEndianBytes(BigInteger value, int length) {
        byte[] bytes = value.toByteArray();

        // Handle sign byte
        if (bytes.length == length + 1 && bytes[0] == 0) {
            byte[] result = new byte[length];
            System.arraycopy(bytes, 1, result, 0, length);
            return result;
        }

        // Pad with zeros if needed
        if (bytes.length < length) {
            byte[] result = new byte[length];
            System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
            return result;
        }

        // Truncate if needed (shouldn't happen in normal cases)
        if (bytes.length > length) {
            byte[] result = new byte[length];
            System.arraycopy(bytes, bytes.length - length, result, 0, length);
            return result;
        }

        return bytes;
    }

    /**
     * Converts a byte array to a hex string with 0x prefix
     *
     * @param bytes The byte array
     * @return The hex string
     */
    public static String toHexString(byte[] bytes) {
        return "0x" + Numeric.toHexStringNoPrefix(bytes);
    }
}