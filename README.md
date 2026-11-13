# SpendWise – Personal Expense Management System

> **Core Java | OOP | JDBC | MySQL | HTML | CSS | JavaScript | JUnit 5**

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue.svg)](https://www.mysql.com/)
[![Architecture](https://img.shields.io/badge/Architecture-3--Tier%20MVC-brightgreen.svg)]()
[![Testing](https://img.shields.io/badge/Testing-JUnit%205%20(15%20Passed)-success.svg)]()
[![Interface](https://img.shields.io/badge/Interface-HTML5%20%7C%20CSS3%20%7C%20JS-purple.svg)]()

---

## 📌 Executive Summary & Architecture

**SpendWise** is a full-phased, production-grade personal financial management system engineered in **Core Java**. It models realistic personal finance operations—including capital inflow tracking, expense categorization, monthly budget threshold monitoring, and financial health analytics.

### Architectural Decision
- **Application Logic**: Handled entirely by **Core Java (Java SE 17)** utilizing clean Object-Oriented Principles, multi-layered service separation, and domain encapsulation.
- **Database Connectivity**: **JDBC (Java Database Connectivity)** executing parameterized `PreparedStatement` transactions against **MySQL 8.0** to guarantee strict data integrity and prevent SQL injection.
- **User Interface**: A responsive, modern financial dashboard built with **HTML5, CSS3 (Modern Dark Theme), and Vanilla JavaScript**, served directly by an embedded Core Java HTTP server (`com.sun.net.httpserver.HttpServer`).
- **Testing**: Comprehensive **JUnit 5** test suite validating OOP models, polymorphic behavior, and domain calculations.

```
+--------------------------------------------------------------------------+
|                 PRESENTATION LAYER (HTML5 / CSS3 / Vanilla JS)           |
|      - Responsive Financial Dashboard        - Monthly Budget Visualizer |
|      - Transaction Manager & Dynamic Filters - Analytics & Summary Cards |
+------------------------------------+-------------------------------------+
                                     | HTTP REST (JSON)
                                     v
+--------------------------------------------------------------------------+
|                  CORE JAVA BACKEND (Embedded HttpServer)                 |
|   com.spendwise.web (AuthHandler, TransactionHandler, BudgetHandler)     |
+------------------------------------+-------------------------------------+
                                     | Service Invocations
                                     v
+--------------------------------------------------------------------------+
|                         BUSINESS SERVICE LAYER                           |
|       IUserService            ITransactionService        IBudgetService  |
|     (UserServiceImpl)      (TransactionServiceImpl)   (BudgetServiceImpl)|
+------------------------------------+-------------------------------------+
                                     | Polymorphic Domain Dispatch
                                     v
+--------------------------------------------------------------------------+
|                        DATA ACCESS OBJECT (DAO)                          |
|         IUserDAO               ITransactionDAO            IBudgetDAO     |
|      (UserDAOImpl)          (TransactionDAOImpl)       (BudgetDAOImpl)   |
+------------------------------------+-------------------------------------+
                                     | JDBC (PreparedStatement / Pooling)
                                     v
+--------------------------------------------------------------------------+
|                           DATABASE LAYER                                 |
|                       MySQL 8.0 (spendwise DB)                           |
|            [users]  <---(FK)--->  [transactions]  |  [budgets]           |
+--------------------------------------------------------------------------+
```

---

## 🏛️ Deep-Dive: Object-Oriented Programming (OOP) Implementation

This project was built to illustrate real-world mastery of the **Four Pillars of OOP**:

### 1. Encapsulation
- **Sensitive Data Protection**: Confidential fields in `User` (`passwordHash`, `salt`) are strictly `private`. Passwords are encrypted using SHA-256 with cryptographically generated 16-byte salts (`PasswordUtil`). Plain text passwords are never stored or exposed.
- **Controlled Account Invariants**: In `Account`, the `balance` field cannot be modified arbitrarily via direct setters. All mutations must flow through domain methods `deposit(double amount)` and `withdraw(double amount)` which validate that amount > 0 and update the balance atomically.
- **Budget Boundaries**: `Budget` protects `monthlyLimit` and `currentSpent`, exposing computed domain metrics `getRemainingBudget()`, `getPercentageUsed()`, `isWarning()` (triggered at 80% utilization), and `isExceeded()` (triggered when spent > limit).

### 2. Inheritance
- **Base Entities**: `BaseEntity` establishes common identification and audit timestamps (`id`, `createdAt`, `updatedAt`).
- **User Reusability**: `BaseUser` extends `BaseEntity` to encapsulate common identity fields (`fullName`, `email`) and format verification regex. Concrete `User` inherits `BaseUser` while extending it with credential security attributes.
- **Transaction Taxonomy**: `AbstractTransaction` extends `BaseEntity`, providing shared attributes (`userId`, `amount`, `category`, `description`, `transactionDate`) inherited by concrete transaction types.
- **Base DAO Architecture**: `BaseDAO` encapsulates common JDBC connection acquisition, error wrapping, and safe resource closure (`AutoCloseable...`).

### 3. Polymorphism
- **Dynamic Balance Processing**: `AbstractTransaction` declares the abstract method:
  ```java
  public abstract void processAccount(Account account);
  public abstract double getSignedAmount();
  ```
- **Concrete Subtype Behavior**:
  - `IncomeTransaction`: Polymorphically returns `+amount` and invokes `account.deposit(amount)`.
  - `ExpenseTransaction`: Polymorphically returns `-amount` and invokes `account.withdraw(amount)`.
- **Elimination of Conditional Branching**: In `TransactionServiceImpl.calculateAccountBalance()`, transactions are processed via runtime dynamic dispatch:
  ```java
  for (AbstractTransaction tx : allTx) {
      tx.processAccount(account); // Dispatches polymorphically without if-else!
  }
  ```

### 4. Abstraction
- **Contract-First Interfaces**: Core services and data layers are decoupled via interfaces:
  - `IUserService`, `ITransactionService`, `IBudgetService`
  - `IUserDAO`, `ITransactionDAO`, `IBudgetDAO`
- **Factory Pattern**: `TransactionFactory` abstracts the creation of concrete transaction instances based on string or enum types, hiding instantiation complexity from controllers and DAOs.

---

## ✨ Features Breakdown

| # | Feature | Technical Implementation |
|---|---|---|
| 1 | **User Registration & Auth** | Salted SHA-256 password hashing, email validation regex, credential verification, and user session state. |
| 2 | **Income & Expense Management** | Full CRUD operations. Polymorphic balance adjustment for deposits and debits. |
| 3 | **Category-wise Tracking** | Standard income streams (*Salary, Freelance, Business, Investments*) and expense classifications (*Food, Housing, Utilities, Transportation, Entertainment, etc.*). |
| 4 | **Monthly Budget Management** | Per-category spending caps. Real-time consumption calculation with **Safe (Green)**, **Warning at ≥80% (Amber)**, and **Exceeded (Red)** status indicators. |
| 5 | **Transaction History & Filtering** | Multi-attribute filtering by **Transaction Type (All/Income/Expense)**, **Category**, **Date Range**, and **Keyword search** across descriptions. |
| 6 | **Monthly Expense Summaries** | Aggregate calculations of Total Income, Total Expenses, Net Savings, and Savings Rate percentage, accompanied by category breakdown bars. |
| 7 | **Validation & Custom Exceptions** | Clean exception hierarchy (`ValidationException`, `AuthenticationException`, `ResourceNotFoundException`, `BudgetExceededException`, `DatabaseException`). |

---

## 🗄️ Database Design (MySQL)

### Entity Relationship & Tables
```sql
-- 1. Users Table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
);

-- 2. Transactions Table
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type ENUM('INCOME', 'EXPENSE') NOT NULL,
    category VARCHAR(50) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_transactions_user_date (user_id, transaction_date),
    INDEX idx_transactions_user_category (user_id, category),
    INDEX idx_transactions_user_type (user_id, type)
);

-- 3. Monthly Budgets Table
CREATE TABLE budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category VARCHAR(50) NOT NULL,
    monthly_limit DECIMAL(12, 2) NOT NULL,
    month TINYINT NOT NULL CHECK (month BETWEEN 1 AND 12),
    year SMALLINT NOT NULL CHECK (year >= 2000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_user_category_month_year UNIQUE (user_id, category, month, year),
    INDEX idx_budgets_user_period (user_id, month, year)
);
```

---

## 🧪 Testing Suite (JUnit 5)

The project includes unit tests covering business logic, domain boundaries, and OOP validation:
- **`UserEncapsulationTest`**: Validates email format validation, empty name prevention, salt generation, and cryptographic verification.
- **`TransactionPolymorphismTest`**: Validates signed amount calculations, `TransactionFactory` dispatch, and polymorphic `Account` mutations.
- **`BudgetDomainTest`**: Verifies remaining budget calculations, warning status triggers at 80%, and exceeded status flags.
- **`ValidationUtilTest`**: Boundary tests for negative amounts, null dates, and invalid year/month ranges.

### Test Execution Command
```bash
# Windows
test.bat

# Linux / macOS
./scripts/test.sh
```

**JUnit Console Output**:
```
+-- JUnit Jupiter [OK]
| +-- BudgetDomainTest [OK] (3/3 tests passed)
| +-- TransactionPolymorphismTest [OK] (4/4 tests passed)
| +-- UserEncapsulationTest [OK] (4/4 tests passed)
| '-- ValidationUtilTest [OK] (4/4 tests passed)
[ 15 tests found | 15 tests successful | 0 tests failed ]
```

---

## 🚀 Quick Start & Execution Guide

### Prerequisites
- **Java SE Development Kit (JDK 17+)**
- **MySQL Server 8.0+** (Optional: application automatically switches to in-memory fallback if MySQL is offline, guaranteeing immediate runnable evaluation)
- **Git**

### 1. Clone Repository
```bash
git clone https://github.com/<your-username>/SpendWise.git
cd SpendWise
```

### 2. Configure Database (Optional)
Edit `src/main/resources/db.properties` with your MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/spendwise?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
db.username=root
db.password=your_mysql_password
```

### 3. Compile Project
```cmd
# Windows
build.bat

# Linux / macOS
chmod +x scripts/*.sh
./scripts/build.sh
```

### 4. Run Application
```cmd
# Windows
run.bat

# Linux / macOS
./scripts/run.sh
```

### 5. Access Web Dashboard
Open your web browser and navigate to:
```
http://localhost:8080/
```

> **Quick Evaluator Login**:
> - Email: `demo@spendwise.com`
> - Password: `password123`
> *(Or click the **1-Click Fill** button on the sign-in page to load pre-seeded demo records!)*

---

## 📂 Project Directory Structure

```
SpendWise/
├── src/
│   ├── main/
│   │   ├── java/com/spendwise/
│   │   │   ├── Main.java                        # Entry point & bootstrapper
│   │   │   ├── config/DatabaseConfig.java       # Properties reader
│   │   │   ├── db/
│   │   │   │   ├── DBConnection.java            # JDBC connection manager
│   │   │   │   └── DatabaseInitializer.java     # Automatic DDL execution
│   │   │   ├── model/
│   │   │   │   ├── BaseEntity.java              # Inheritance base
│   │   │   │   ├── BaseUser.java                # User identity base
│   │   │   │   ├── User.java                    # Encapsulation
│   │   │   │   ├── Account.java                 # Balance domain encapsulation
│   │   │   │   ├── AbstractTransaction.java     # Abstraction & Polymorphism base
│   │   │   │   ├── IncomeTransaction.java       # Polymorphic deposit behavior
│   │   │   │   ├── ExpenseTransaction.java      # Polymorphic withdrawal behavior
│   │   │   │   ├── TransactionFactory.java      # Factory pattern
│   │   │   │   ├── TransactionType.java         # Classification enum
│   │   │   │   ├── Budget.java                  # Budget domain logic
│   │   │   │   ├── Category.java                # Category catalogs
│   │   │   │   └── MonthlySummary.java          # Aggregates DTO
│   │   │   ├── dao/
│   │   │   │   ├── BaseDAO.java                 # Shared JDBC lifecycle
│   │   │   │   ├── IUserDAO.java                # User DAO Interface
│   │   │   │   ├── UserDAOImpl.java             # JDBC User implementation
│   │   │   │   ├── ITransactionDAO.java         # Transaction DAO Interface
│   │   │   │   ├── TransactionDAOImpl.java      # JDBC Transaction queries
│   │   │   │   ├── IBudgetDAO.java              # Budget DAO Interface
│   │   │   │   └── BudgetDAOImpl.java           # JDBC Budget upsert queries
│   │   │   ├── service/
│   │   │   │   ├── IUserService.java            # Auth & User service
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   ├── ITransactionService.java     # Transaction service
│   │   │   │   ├── TransactionServiceImpl.java
│   │   │   │   ├── IBudgetService.java          # Budget service
│   │   │   │   └── BudgetServiceImpl.java
│   │   │   ├── exception/                       # Custom exception hierarchy
│   │   │   ├── util/
│   │   │   │   ├── PasswordUtil.java            # Salted SHA-256 security
│   │   │   │   ├── ValidationUtil.java          # Regex & boundary validation
│   │   │   │   └── JsonUtil.java                # Zero-dependency Core Java JSON parser
│   │   │   └── web/
│   │   │       ├── HttpServerApp.java           # Embedded HTTP server
│   │   │       ├── HttpUtils.java               # CORS & Request parsers
│   │   │       └── handler/                     # REST API & static file handlers
│   │   └── resources/
│   │       ├── schema.sql                       # Complete MySQL DDL schema
│   │       ├── db.properties                    # Database configuration
│   │       └── web/                             # HTML/CSS/JavaScript client
│   │           ├── index.html                   # Auth & landing interface
│   │           ├── dashboard.html               # Main financial dashboard
│   │           ├── css/style.css                # Modern dark responsive theme
│   │           └── js/
│   │               ├── api.js                   # REST API client
│   │               ├── auth.js                  # Authentication logic
│   │               └── app.js                   # Dashboard state & UI updates
│   └── test/java/com/spendwise/                 # JUnit 5 test suite
├── lib/                                         # Project dependencies (standalone execution)
│   ├── mysql-connector-j-8.3.0.jar
│   └── junit-platform-console-standalone-1.10.2.jar
├── scripts/                                     # Cross-platform build & run scripts
├── build.bat / run.bat / test.bat               # 1-click Windows shortcuts
├── pom.xml                                      # Maven POM for IDE integration
├── .gitignore
└── README.md
```

---

## 💼 Resume / CV Project Bullet Points

Use these high-impact bullet points for your Tech Resume / CV:

```markdown
SpendWise – Personal Expense Management System
Core Java | Object-Oriented Programming (OOP) | JDBC | MySQL | HTML5 | CSS3 | JavaScript | JUnit 5

• Engineered a full-phased financial tracking web application using Core Java (JDK 17) and 3-Tier MVC architecture, eliminating external framework overhead with an embedded Java HTTP server.
• Implemented core OOP principles: encapsulated financial balances and salted SHA-256 password security, utilized inheritance across reusable base entities, and leveraged polymorphism for dynamic balance processing across Income and Expense transactions.
• Designed and indexed a MySQL 8.0 relational schema with foreign key cascades, managing data access using JDBC PreparedStatements to prevent SQL injection vulnerabilities.
• Built a responsive, modern dark-themed web dashboard in HTML5, CSS3, and Vanilla JavaScript with real-time budget threshold warnings (≥80% and >100%) and multi-attribute transaction filtering.
• Authored a 15-test JUnit 5 automated test suite achieving 100% pass rate across domain models, factory instantiation, threshold alerts, and custom validation exceptions.
```

---

## 👩‍💻 Author
**Suchismita Saha**  
*SpendWise – Personal Expense Management System*
