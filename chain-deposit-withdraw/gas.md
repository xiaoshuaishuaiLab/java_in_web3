# 基于EIP-1559 和infura的api，构建一个以太坊转账交易时需要BigInteger maxPriorityFeePerGas, BigInteger maxFeePerGas，当前的交易场景为重要不紧急，应该如何构建上述两个参数呢

你好！很高兴能帮助你构建基于 EIP-1559 和 Infura API 的以太坊交易。对于“重要不紧急”的交易场景，合理设置 `maxPriorityFeePerGas` 和 `maxFeePerGas` 是关键，既能确保交易被打包，又能避免支付过高的费用。

-----

## EIP-1559 基础概念回顾

在 EIP-1559 中，交易费用由三部分组成：

1.  **`baseFeePerGas` (基础费用):** 这是协议自动计算的，由网络拥堵情况决定，并且会在每个区块中波动。这部分费用会被销毁。
2.  **`maxPriorityFeePerGas` (优先费/小费):** 这是你愿意支付给矿工的额外小费，用于激励他们优先打包你的交易。这部分费用归矿工所有。
3.  **`maxFeePerGas` (最高费用):** 这是你愿意为每单位 Gas 支付的最高总费用。它必须大于或等于 `baseFeePerGas + maxPriorityFeePerGas`。如果 `maxFeePerGas` 大于 `baseFeePerGas + maxPriorityFeePerGas`，那么超出部分将退还给你。

-----

## “重要不紧急”场景下参数构建策略

对于“重要不紧急”的交易，我们的目标是让交易在合理的时间内被打包，但同时尽量降低成本。这意味着我们不追求立即打包，但也不希望交易长时间停留在内存池中。

### 1\. `maxPriorityFeePerGas` 的构建

由于是“重要不紧急”的交易，我们可以设置一个相对较低但仍然能够吸引矿工的 `maxPriorityFeePerGas`。

* **获取推荐值：** 最佳实践是查询当前网络的推荐优先费用。Infura 不直接提供这个 API，但你可以使用像 **ethers.js** 或 **web3.js** 这样的库来查询历史区块或使用第三方 Gas 预估服务来获取推荐的 
* 
* **手动设置策略：**
    * **一个保守但有效的起始点是 1 Gwei 或 2 Gwei。** 这通常足以让交易在不那么拥堵的网络中被打包。
    * **观察网络状况：** 如果你发现交易长时间未被打包，可以考虑稍后重新提交一个更高 `maxPriorityFeePerGas` 的交易（或使用加速交易功能）。
    * **避免过高：** 由于不紧急，我们没必要支付过高的优先费。

### 2\. `maxFeePerGas` 的构建

`maxFeePerGas` 应该足够高以覆盖 `baseFeePerGas` 和你的 `maxPriorityFeePerGas`，并且预留一些空间以应对 `baseFeePerGas` 的短期波动。

* **获取当前的 `baseFeePerGas`：** 你可以从 Infura API 获取当前区块的 `baseFeePerGas`。
    * **示例 (ethers.js):**
      ```javascript
      const provider = new ethers.providers.InfuraProvider("mainnet", "YOUR_INFURA_PROJECT_ID");
      const block = await provider.getBlock('latest');
      const currentBaseFeePerGas = block.baseFeePerGas;
      console.log("当前的 baseFeePerGas:", currentBaseFeePerGas.toString());
      ```
* **计算 `maxFeePerGas`：**
    * 一个常用的策略是：`maxFeePerGas = (当前 baseFeePerGas * 1.15) + 你的 maxPriorityFeePerGas`。这里的 `1.15` 是一个安全边际，表示你愿意为 `baseFeePerGas` 支付比当前值高 15% 的费用，以应对短期上涨。
    * **重要提示：** 所有费用参数都应该是 **BigInteger** 类型。

-----

## 总结与建议

* **`maxPriorityFeePerGas`:** 对于“重要不紧急”的交易，可以尝试设置一个较低的值，例如 **1 Gwei 到 3 Gwei**。具体数值可以通过 `provider.getFeeData()` 获取推荐值后，再根据你的需求进行微调。
* **`maxFeePerGas`:** 确保它 **大于等于 `baseFeePerGas + maxPriorityFeePerGas`**。一个安全的做法是，在当前的 `baseFeePerGas` 基础上增加 **10%-20%** 的缓冲，再加上你设置的 `maxPriorityFeePerGas`。
* **使用 BigInteger (或 BigNumber):** 在 JavaScript 中，由于以太坊的费用单位非常小（wei），并且数值可能非常大，务必使用 `BigInteger` 或 ethers.js/web3.js 提供的 `BigNumber` 类型来处理这些数值，避免浮点数精度问题。
* **监控与调整：** 即使设置了参数，也建议在发送交易后监控其状态。如果交易长时间未被打包，可能需要考虑提高费用或重新发送交易。