package com.johnnydemo.frauddetector.service;

import com.johnnydemo.frauddetector.entity.Transaction;
import com.johnnydemo.frauddetector.mapper.TransactionMapper;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private KieSession kieSession;

    public void detectFraud(Transaction transaction) {
        kieSession.insert(transaction);
        kieSession.fireAllRules();

        // 获取当前时间的 ZonedDateTime 对象，时区为上海
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        ZonedDateTime nowInShanghai = ZonedDateTime.now(zoneId);

        // 转换为 Timestamp
        Timestamp nowInShanghaiTimestamp = Timestamp.from(nowInShanghai.toInstant());
        System.out.println(nowInShanghaiTimestamp);

        // 设置 createdAt 和 updatedAt
        transaction.setCreatedAt(nowInShanghaiTimestamp);
        transaction.setUpdatedAt(nowInShanghaiTimestamp);
        transactionMapper.insert(transaction);
    }
}