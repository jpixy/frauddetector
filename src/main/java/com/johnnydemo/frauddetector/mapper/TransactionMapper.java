package com.johnnydemo.frauddetector.mapper;

import com.johnnydemo.frauddetector.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransactionMapper {

    int insert(Transaction transaction);

    Transaction selectById(@Param("id") Long id);

    List<Transaction> selectAll();

    int update(Transaction transaction);

    int deleteById(@Param("id") Long id);
}