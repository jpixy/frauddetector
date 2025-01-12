package com.johnnydemo.frauddetector.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
public class Transaction {
    private Long id;
    private String accountNumberFrom;
    private String accountNumberTo;
    private String accountLocation;
    private String transactionLocation;
    private BigDecimal amount;
    private String transactionType;
    private boolean isFraud;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}