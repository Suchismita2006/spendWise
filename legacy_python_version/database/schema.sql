

CREATE DATABASE IF NOT EXISTS spendwise;

USE spendwise;


CREATE TABLE IF NOT EXISTS users (

    user_id INT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

);


CREATE TABLE IF NOT EXISTS transactions (

    transaction_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,

    type ENUM('income', 'expense') NOT NULL,

    category VARCHAR(50) NOT NULL,

    amount DECIMAL(12, 2) NOT NULL,

    description VARCHAR(255),

    transaction_date DATE NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    INDEX idx_transactions_user_date (
        user_id,
        transaction_date
    ),

    INDEX idx_transactions_user_category (
        user_id,
        category
    )

);


CREATE TABLE IF NOT EXISTS budgets (

    budget_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,

    category VARCHAR(50) NOT NULL,

    amount DECIMAL(12, 2) NOT NULL,

    month TINYINT NOT NULL,

    year SMALLINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT unique_user_category_budget
        UNIQUE (
            user_id,
            category,
            month,
            year
        )

);


CREATE TABLE IF NOT EXISTS savings_goals (

    goal_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,

    goal_name VARCHAR(100) NOT NULL,

    target_amount DECIMAL(12, 2) NOT NULL,

    saved_amount DECIMAL(12, 2)
        NOT NULL
        DEFAULT 0.00,

    target_date DATE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_goals_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE

);


SHOW TABLES;
