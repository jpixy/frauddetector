package com.johnnydemo.frauddetector;

import com.johnnydemo.frauddetector.entity.Transaction;
import com.johnnydemo.frauddetector.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class FraudDetectorApplicationTests {
    @Autowired
    private TransactionService transactionService;

    @Test
    public void testHighAmountTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("15000.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 true
        assert transaction.isFraud();
    }

    @Test
    public void testMultipleTransactionsInShortTime() {
        Transaction t1 = new Transaction();
        t1.setAccountNumberFrom("1234567890");
        t1.setAccountNumberTo("0987654321");
        t1.setAmount(new BigDecimal("1000.00"));
        t1.setTransactionType("DEBIT");
        t1.setStatus("PENDING");

        Transaction t2 = new Transaction();
        t2.setAccountNumberFrom("1234567890");
        t2.setAccountNumberTo("0987654321");
        t2.setAmount(new BigDecimal("2000.00"));
        t2.setTransactionType("DEBIT");
        t2.setStatus("PENDING");

        transactionService.detectFraud(t1);
        transactionService.detectFraud(t2);

        // 断言 t1.isFraud() 和 t2.isFraud() 为 true
        assert t1.isFraud();
        assert t2.isFraud();
    }
}