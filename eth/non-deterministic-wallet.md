# Ethereum 1.0 Keystore File
## 各个字段的含义



这是一个以太坊非确定性（随机生成）钱包的 keystore 文件，常见于 web3j、geth 等工具生成。

```
{
  "address": "fddd42647c10fc21079581cca0c4a8763f8b64f0",
  "id": "fbaceee3-e4dd-4aaf-b15c-202ec03e6d00",
  "version": 3,
  "crypto": {
    "cipher": "aes-128-ctr",
    "ciphertext": "5621ef213aa2dc3dad3cd99359bcdf048bafc671c16aa7dd5c398002797626cf",
    "cipherparams": {
      "iv": "17abfe9bcff7e3059b53fb73c155a041"
    },
    "kdf": "scrypt",
    "kdfparams": {
      "dklen": 32,
      "n": 262144,
      "p": 1,
      "r": 8,
      "salt": "449747dfcdb6963c9d88d5dc6789fbb239bdab1f8e612a0b92a0bd3720282c5c"
    },
    "mac": "b8c1797fa8f660d0c923545380eff471a4abca959723168cdefd2488a0b4f671"
  }
}
```



各字段含义如下：

- `address`：钱包地址（去掉了 0x 前缀），由公钥推导而来。
- `id`：钱包文件的唯一标识（UUID），用于区分不同钱包文件。
- `version`：keystore 文件格式的版本号，当前为 3。
- `crypto`：加密相关信息，包含以下子字段：
    - `cipher`：加密算法，这里是 `aes-128-ctr`。
    - `ciphertext`：用密码加密后的私钥密文。
    - `cipherparams`：加密参数，包含：
        - `iv`：初始化向量（IV），用于加密算法。
    - `kdf`：密钥派生函数（Key Derivation Function），这里是 `scrypt`，用于从用户密码生成加密密钥。
    - `kdfparams`：密钥派生参数，包含：
        - `dklen`：派生密钥长度（字节数）。
        - `n`、`p`、`r`：scrypt 算法的参数，影响计算复杂度和安全性。
        - `salt`：加盐值，增加破解难度。
    - `mac`：消息认证码（Message Authentication Code），用于校验密码和数据完整性，防止篡改。

该文件用于安全地存储以太坊私钥，只有输入正确密码才能解密恢复私钥。

### 生成方式

String fileName = WalletUtils.generateNewWalletFile("", keystoreDir, true);
对应的fileName对应的 keystore 文件内容即上述json

## 获取私钥
String password = ""; // 空密码
String keystorePath = "eth/1.json"; // keystore 文件路径

 Credentials credentials = WalletUtils.loadCredentials(password, new File(keystorePath));
 System.out.println("私钥: " + credentials.getEcKeyPair().getPrivateKey().toString(16));
 System.out.println("地址: " + credentials.getAddress());





