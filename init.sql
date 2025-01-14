CREATE USER 'remote_user'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON *.* TO 'remote_user'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


-- 创建数据库
CREATE DATABASE IF NOT EXISTS demo_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用创建的数据库
USE demo_db;

-- 创建表
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number_from VARCHAR(255),
    account_number_to VARCHAR(255),
    account_location VARCHAR(255),
    transaction_location VARCHAR(255),
    amount DECIMAL(19, 4),
    transaction_type VARCHAR(50),
    is_fraud BOOLEAN,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;

SHOW CREATE TABLE transactions;

INSERT INTO transactions (account_number_from, account_number_to, account_location, transaction_location, amount, transaction_type, is_fraud, status) VALUES
('1234567890', '0987654321', 'New York', 'Los Angeles', 100.00, 'Transfer', FALSE, 'Completed'),
('2345678901', '1234567890', 'Chicago', 'Houston', 200.50, 'Deposit', FALSE, 'Pending'),
('3456789012', '2345678901', 'San Francisco', 'Seattle', 150.75, 'Withdrawal', TRUE, 'Failed');

SELECT * FROM transactions;

