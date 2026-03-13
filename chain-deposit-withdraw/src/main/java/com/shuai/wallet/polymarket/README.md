# ConditionalTokens Java Implementation

This package contains Java implementations of the Polymarket ConditionalTokens contract helper functions.

## Overview

The `ConditionalTokensHelper` class provides Java implementations of key functions from the ConditionalTokens smart contract, specifically focusing on the CTHelpers library methods.

## Key Methods

### 1. getConditionId

Constructs a condition ID from an oracle address, question ID, and outcome slot count.

```java
String oracle = "0x1234567890123456789012345678901234567890";
byte[] questionId = new byte[32]; // Your question ID
long outcomeSlotCount = 2; // Number of possible outcomes

byte[] conditionId = ConditionalTokensHelper.getConditionId(oracle, questionId, outcomeSlotCount);
```

### 2. getCollectionId

Constructs an outcome collection ID from a parent collection and an outcome collection. This method uses elliptic curve cryptography (BN128 curve).

```java
byte[] parentCollectionId = new byte[32]; // bytes32(0) for no parent
byte[] conditionId = ...; // From getConditionId
BigInteger indexSet = BigInteger.valueOf(1); // Outcome index set (bit mask)

byte[] collectionId = ConditionalTokensHelper.getCollectionId(
    parentCollectionId,
    conditionId,
    indexSet
);
```

### 3. getPositionId

Constructs a position ID from a collateral token and an outcome collection. This ID is used as the ERC-1155 token ID.

```java
String collateralToken = "0xabcdefabcdefabcdefabcdefabcdefabcdefabcd";
byte[] collectionId = ...; // From getCollectionId

BigInteger positionId = ConditionalTokensHelper.getPositionId(collateralToken, collectionId);
```

## Understanding Index Sets

Index sets are bit masks representing outcome combinations:

For a condition with 3 outcomes (A, B, C):
- `0b001` (1) = Outcome A
- `0b010` (2) = Outcome B
- `0b100` (4) = Outcome C
- `0b011` (3) = Outcome A OR B
- `0b111` (7) = All outcomes (A OR B OR C)

Example:
```java
// For a binary outcome (YES/NO)
BigInteger yesOutcome = BigInteger.valueOf(0b01); // 1
BigInteger noOutcome = BigInteger.valueOf(0b10);  // 2
BigInteger bothOutcomes = BigInteger.valueOf(0b11); // 3

// For three outcomes
BigInteger outcomeA = BigInteger.valueOf(0b001); // 1
BigInteger outcomeB = BigInteger.valueOf(0b010); // 2
BigInteger outcomeC = BigInteger.valueOf(0b100); // 4
BigInteger outcomeAorB = BigInteger.valueOf(0b011); // 3
```

## Important Notes

### Elliptic Curve Implementation

The `getCollectionId` method involves complex elliptic curve operations on the BN128 curve. The current implementation includes:

1. **Simplified sqrt operation**: The Solidity contract uses a highly optimized custom square root algorithm specific to the BN128 curve's prime field. The Java implementation uses a simplified version that may not produce identical results in all cases.

2. **Point addition**: The elliptic curve point addition is implemented, but for production use, you should:
   - Use a battle-tested elliptic curve library
   - Or call the smart contract directly to get accurate results
   - Or use the precompiled contract (address 6) via RPC calls

### Production Recommendations

For production use, consider these approaches:

#### Option 1: Call the Smart Contract Directly
```java
// Use Web3j to call the contract's getCollectionId function
ConditionalTokens contract = ConditionalTokens.load(
    contractAddress,
    web3j,
    credentials,
    gasPrice,
    gasLimit
);

byte[] collectionId = contract.getCollectionId(
    parentCollectionId,
    conditionId,
    indexSet
).send();
```

#### Option 2: Use a Proper EC Library

Consider using a library like Bouncy Castle for accurate BN128 curve operations:

```java
// Add dependency
// implementation 'org.bouncycastle:bcprov-jdk15on:1.70'
```

#### Option 3: Cache Results

If you're working with known markets, cache the collection IDs from on-chain data:

```java
Map<String, byte[]> collectionIdCache = new HashMap<>();
String key = parentCollectionId + conditionId + indexSet;
byte[] collectionId = collectionIdCache.computeIfAbsent(key,
    k -> fetchFromContract(parentCollectionId, conditionId, indexSet)
);
```

## Testing

Run the test suite to see examples:

```bash
mvn test -Dtest=ConditionalTokensHelperTest
```

## Example Usage with Polymarket

```java
// Step 1: Get condition information from Polymarket API
String oracle = "0x..."; // Polymarket oracle address
byte[] questionId = ...; // From market data
long outcomeSlotCount = 2; // Binary market (YES/NO)

// Step 2: Calculate condition ID
byte[] conditionId = ConditionalTokensHelper.getConditionId(
    oracle,
    questionId,
    outcomeSlotCount
);

// Step 3: Get collection IDs for each outcome
byte[] yesCollectionId = ConditionalTokensHelper.getCollectionId(
    new byte[32], // No parent
    conditionId,
    BigInteger.valueOf(1) // YES outcome
);

byte[] noCollectionId = ConditionalTokensHelper.getCollectionId(
    new byte[32], // No parent
    conditionId,
    BigInteger.valueOf(2) // NO outcome
);

// Step 4: Get position IDs (ERC-1155 token IDs)
String usdcAddress = "0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174"; // USDC on Polygon
BigInteger yesPositionId = ConditionalTokensHelper.getPositionId(usdcAddress, yesCollectionId);
BigInteger noPositionId = ConditionalTokensHelper.getPositionId(usdcAddress, noCollectionId);

// Step 5: Use position IDs to interact with the ConditionalTokens contract
// These IDs can be used with ERC-1155 balanceOf, transfer, etc.
```

## Polymarket Contract Addresses

- **Polygon Mainnet ConditionalTokens**: `0x4D97DCd97eC945f40cF65F87097ACe5EA0476045`
- **Polygon USDC (Collateral)**: `0x2791Bca1f2de4661ED88A30C99A7a9449Aa84174`

## References

- [Polymarket Documentation](https://docs.polymarket.com/)
- [ConditionalTokens Contract](https://github.com/gnosis/conditional-tokens-contracts)
- [EIP-1155: Multi Token Standard](https://eips.ethereum.org/EIPS/eip-1155)
- [BN128 Curve](https://hackmd.io/@jpw/bn254)

## Limitations

1. The square root operation uses a simplified algorithm that may not match Solidity's optimized version
2. The elliptic curve point addition should be verified against known test vectors
3. For critical financial operations, always verify results against on-chain contract calls
4. This implementation is for educational and development purposes

## License

This code is provided as-is for educational purposes.