# DefaultBlockParameterName枚举值分别是什么含义

让我解释一下获取ETH余额时使用的`DefaultBlockParameterName`枚举值的含义：

1. `EARLIEST` - "earliest"
- 表示获取创世区块(第一个区块)时该地址的余额
- 区块高度 = 0

2. `LATEST` - "latest"
- 表示获取最新已确认区块时该地址的余额
- 是最常用的选项

3. `PENDING` - "pending"
- 表示获取当前待处理区块中该地址的余额
- 包含已提交但尚未被确认的交易

4. `FINALIZED` - "finalized"
- 表示获取最新已最终确认区块时的余额
- 在PoS共识下，已经不可能被回滚的区块

5. `SAFE` - "safe"
- 表示获取最新安全区块时的余额
- 在PoS共识下，有较高确定性但理论上仍可能被回滚

6. `ACCEPTED` - "accepted"
- 表示获取最新被接受区块时的余额
- 主要用于一些特定网络

一般情况下：
- 如果需要最新状态，使用 `LATEST`
- 如果需要最终确认状态，使用 `FINALIZED`
- 如果需要包含待处理交易，使用 `PENDING`