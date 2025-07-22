# Java Swing Authentication System

This project is a **robust Java Swing-based authentication system** built with **Maven** and backed by a **MySQL** database. It simulates a real-world, multi-page login flow with secure account creation, password management, and email-based verification. The system is designed with strong security practices, using **Argon2** for password hashing and **SHA-256** for token protection, and demonstrates deep integration across the frontend, backend, and database layers.

---

### 🔐 Key Features

- **Account Creation with Email Verification**  
  New users must create an account using their email. A unique verification token is emailed to them. Users have:
  - 3 attempts to enter the correct token before a 1-minute lockout (enforced via SQL logic)
  - Option to resend the verification code
  - Passwords stored securely using Argon2; tokens hashed with SHA-256

- **Secure Login Flow**  
  - Checks if email is verified before login proceeds  
  - Password authentication and active session validation  
  - If no valid session, user must pass a built-in **Two-Factor Authentication (2FA)** system  
  - 2FA mimics email verification: token delivery, resends, and 1-minute lockouts after 3 failed attempts

- **"Trust This Device" Option**  
  - Allows the user to mark a device as trusted for 7 days  
  - Requires 2FA once to establish the session, which is then stored and validated on future logins

- **Password Management**  
  - Users can **change passwords** from within their account (requires current password)  
  - Users can **reset forgotten passwords** via email verification, following the same secure flow

---

This system demonstrates careful design of authentication layers, secure credential handling, and thoughtful user experience, complete with error handling and verification safeguards. It's a strong foundation for production-grade authentication in Java-based desktop applications.

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
USE AuthSystem;

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
</details>

---

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

---

### 3. Create the MySQL Database

- Open **MySQL Workbench** or use your terminal.
- Paste and execute the SQL schema provided above or in the directory.
- Update your credentials accordingly.

---

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

---

### 5. Run the Application

Make sure you are in AuthSystem/auth_system. Use Maven to compile and run the project:

```bash
mvn clean install
mvn exec:java
```
This will launch the Swing GUI and begin the authentication system.

---

## Future Improvements

- Mostly bug fixing

---

## License

This project is open-source. You may use and modify it for educational purposes.

---

# Acknowledgments

Created by Colin Kula as a learning project for authentication and secure login systems.

colin.kulaa@gmail.com