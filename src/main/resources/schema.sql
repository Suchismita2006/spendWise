-- ========================================================
-- SpendWise - Personal Expense Management System
-- Database Schema for MySQL 8.0+
-- ========================================================

CREATE DATABASE IF NOT EXISTS spendwise;
USE spendwise;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    INDEX idx_transactions_user_date (user_id, transaction_date),
    INDEX idx_transactions_user_category (user_id, category),
    INDEX idx_transactions_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Monthly Budgets Table
CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    monthly_limit DECIMAL(12, 2) NOT NULL,
    month TINYINT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year SMALLINT NOT NULL CHECK (year >= 2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT unique_user_category_month_year
        UNIQUE (user_id, category, month, year),
    INDEX idx_budgets_user_period (user_id, month, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Initial Sample Categories Reference (Seed / Verification)
-- Income Categories: Salary, Freelance, Business, Investments, Rental, Gifts, Other
-- Expense Categories: Food & Dining, Rent & Housing, Utilities, Transportation, Healthcare, Entertainment, Shopping, Education, Personal Care, Other
