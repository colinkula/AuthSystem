# Java Swing Authentication System

This project is a **Java Swing-based authentication system** built with **Maven** and backed by a **MySQL** database. It simulates a real-world multi-page login flow, complete with secure account creation, password management, and email-based verification.

---

## Overview

This application demonstrates a multi-layer authentication system with:

- Account creation
- Email verification with temporary codes
- Secure login
- Password change and reset flow
- Two-factor authentication (2FA) based on session trust

The project aims to simulate how authentication works behind the scenes in modern applications. It was inspired by concepts learned while preparing for the **CompTIA Security+** certification exam.

---

## Features

- **Login screen** with:
  - Email + password entry
  - Toggle password visibility
  - “Trust this device for 7 days” feature
  - Buttons to: Log in, Create account, or Change password

- **Create account screen**:
  - Email + password fields
  - Password visibility toggle
  - Register + back-to-login buttons

- **Email verification**:
  - 5-minute temporary codes
  - Max 3 attempts before lockout
  - Reusable across flows (registration, 2FA, password reset)
  - Resend code option

- **Change password screen**:
  - Requires current password, new password, and confirmation
  - Option to reset if user forgot current password

- **Password reset flow**:
  - Initiated via verification code
  - Leads to simplified new password screen

- **Session tracking** for optional 2FA bypass on trusted devices

---

## Technologies Used

- **Java 15**
- **Java Swing** (GUI)
- **Maven** (project and dependency management)
- **MySQL** (database)
- **HikariCP** (connection pooling)
- **Password4j** (secure password hashing)
- **Jakarta Mail** (email support)
- **SLF4J + Logback** (logging)
- **JUnit** (unit testing)

---

## Database Schema

Here is the full SQL schema to create the necessary MySQL database and tables:

<details>
<summary>Click to expand full SQL schema (this file is also included in the directory)</summary>

```sql
-- Create schema
CREATE DATABASE IF NOT EXISTS auth_system;
USE auth_system;

-- users table
CREATE TABLE users (
    user_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- user_sessions table
CREATE TABLE user_sessions (
    session_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    session_token_hash CHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    device_name VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_ephemeral TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- user_tokens table
CREATE TABLE user_tokens (
    verification_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    type ENUM('2FA', 'email', 'login', 'changePass', 'resetPass') NOT NULL DEFAULT '2FA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
);

-- login_attempts table
CREATE TABLE login_attempts (
    attempt_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    email_entered VARCHAR(255),
    success TINYINT(1) NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    type ENUM('2FA', 'email', 'login', 'changePass', 'resetPass') NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE SET NULL,
    INDEX (email_entered)
);

-- lockouts table
CREATE TABLE lockouts (
    email VARCHAR(255) NOT NULL,
    type ENUM('login', '2FA', 'email', 'changePass', 'resetPass') NOT NULL,
    ip_address VARCHAR(45),
    lockout_until TIMESTAMP NOT NULL,
    PRIMARY KEY (email, type)
);
```

## Installation & Setup

### 1. Prerequisites

Make sure you have the following installed:

- Java JDK 15+
- Maven
- MySQL Server
- *(Optional)* MySQL Workbench for GUI
- A Gmail or SMTP-capable email address (for sending verification codes)

---

### 2. Clone the Repository

```bash
git clone https://github.com/colinkula/AuthSystem.git
cd auth_system
```

### 3. Create the MySQL Database

- Open **MySQL Workbench** or use your terminal.
- Paste and execute the SQL schema provided above or in the directory.
- Update your credentials accordingly.

### 4. Configure config.properties

Create a file named config.properties in the resource folder of the project. It should be the same as config.example.properties but with you information:

```config.properties
DB_URL=jdbc:mysql://localhost:3306/auth_system
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password

email.verification.enabled=true
mail.username=your_email@example.com
mail.password=your_email_password
```
Note: If youre using Gmail, you may need to enable App Passwords or allow less secure apps.

### 5. Run the Application

Use Maven to compile and run the project:

```bash
maven clean install
mvn exec:java
```
This will launch the Swing GUI and begin the authentication system.


