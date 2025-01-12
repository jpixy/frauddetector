# Readme

### 注意要点

本例子为了简化以便展示核心逻辑，简化了以下一些地方，如果在生产环境中，需要改进如下几个地方：

- 表结构中的ID，生产环境中考虑使用分布式ID生成。这里简化成数据库自增ID
- 交易表中简化了user和account关系，通常一个user下面有多个accounts的，这里简化成只考虑account的情况。

### 测试命令

```declarative

curl -X GET http://localhost:8080/transactions

curl -X GET http://localhost:8080/transactions/1

curl -X POST http://localhost:8080/transactions \
-H "Content-Type: application/json" \
-d '{
"accountNumberFrom": "1234567890",
"accountNumberTo": "0987654321",
"amount": 100.00,
"transactionType": "DEBIT",
"isFraud": false,
"status": "PENDING"
}'
curl -X PUT http://localhost:8080/transactions/1 \
-H "Content-Type: application/json" \
-d '{
"accountNumberFrom": "1234567890",
"accountNumberTo": "0987654321",
"amount": 150.00,
"transactionType": "CREDIT",
"isFraud": false,
"status": "COMPLETED"
}'

curl -X DELETE http://localhost:8080/transactions/1

```

### 欺诈规则

```declarative


//    高频率交易：
//        使用 collect 关键字收集同一账户在 10 分钟内的交易记录。
//        如果交易记录数量达到 5 次或更多，则标记为欺诈交易，并记录日志。

    异常交易地点：
        检查交易地点是否与账户注册地点不一致。
        如果不一致，则标记为欺诈交易，并记录日志。

    夜间交易：
        检查交易时间是否在夜间（22:00 至 06:00）。
        如果是夜间交易，则标记为欺诈交易，并记录日志。

    大额交易：
        检查交易金额是否超过 10000 元。
        如果超过，则标记为欺诈交易，并记录日志。

```

### 测试代码和期望输出

#### 创建高金额交易记录

```shell
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumberFrom": "1234567890",
    "accountNumberTo": "0987654321",
    "amount": 15000.00,
    "transactionType": "DEBIT",
    "isFraud": false,
    "status": "PENDING"
  }'

High amount transaction detected: 1234567890
```

```declarative
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumberFrom": "1234567890",
    "accountNumberTo": "0987654321",
    "amount": 1000.00,
    "transactionType": "DEBIT",
    "isFraud": false,
    "status": "PENDING"
  }'

curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumberFrom": "1234567890",
    "accountNumberTo": "0987654321",
    "amount": 2000.00,
    "transactionType": "DEBIT",
    "isFraud": false,
    "status": "PENDING"
  }'

Multiple transactions in short time detected: 1234567890


SELECT * FROM transaction WHERE account_number_from = '1234567890';
```

