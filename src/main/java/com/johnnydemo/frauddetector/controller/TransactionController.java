package com.johnnydemo.frauddetector.controller;

import com.johnnydemo.frauddetector.entity.Transaction;
import com.johnnydemo.frauddetector.mapper.TransactionMapper;
import com.johnnydemo.frauddetector.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionMapper transactionMapper;

    // 创建交易记录
    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        transactionService.detectFraud(transaction);
        return transaction;
    }

    // 根据 ID 获取交易记录
    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id) {
        return transactionMapper.selectById(id);
    }

    // 获取所有交易记录
    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionMapper.selectAll();
    }

    // 更新交易记录
    @PutMapping("/{id}")
    public Transaction updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        transaction.setId(id);
        transactionMapper.update(transaction);
        return transaction;
    }

    // 删除交易记录
    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionMapper.deleteById(id);
    }

    // 批量删除交易记录
    @DeleteMapping
    public void deleteTransactions(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            transactionMapper.deleteById(id);
        }
    }
}