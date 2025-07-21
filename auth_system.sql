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
