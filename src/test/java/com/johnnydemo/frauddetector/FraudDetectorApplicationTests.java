package com.johnnydemo.frauddetector;

import com.johnnydemo.frauddetector.entity.Transaction;
import com.johnnydemo.frauddetector.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(transaction.isFraud());
    }

    @Test
    public void testSuspiciousTransactionLocation() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");
        transaction.setAccountLocation("New York");
        transaction.setTransactionLocation("Los Angeles");

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 true
        assertTrue(transaction.isFraud());
    }

    @Test
    public void testNighttimeTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        transaction.setCreatedAt(timestamp);

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 true
        assertTrue(transaction.isFraud());
    }


    @Test
    public void testNormalTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");
        transaction.setAccountLocation("New York");
        transaction.setTransactionLocation("New York");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        transaction.setCreatedAt(timestamp);

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 false
        assertFalse(transaction.isFraud());
    }
    @Test
    public void testMultipleRulesTriggered() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("15000.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");
        transaction.setAccountLocation("New York");
        transaction.setTransactionLocation("Los Angeles");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        transaction.setCreatedAt(timestamp);

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 true
        assertTrue(transaction.isFraud());
    }

    @Test
    public void testAmountEqualToThreshold() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("10000.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 false
        assertFalse(transaction.isFraud());
    }

    @Test
    public void testAmountBelowThreshold() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("9999.99"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 false
        assertFalse(transaction.isFraud());
    }

    @Test
    public void testTransactionLocationSameAsAccountLocation() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumberFrom("1234567890");
        transaction.setAccountNumberTo("0987654321");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setTransactionType("DEBIT");
        transaction.setStatus("PENDING");
        transaction.setAccountLocation("New York");
        transaction.setTransactionLocation("New York");

        transactionService.detectFraud(transaction);

        // 断言 transaction.isFraud() 为 false
        assertFalse(transaction.isFraud());
    }
}