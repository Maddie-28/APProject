CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE IF NOT EXISTS users_auth (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    role ENUM('student', 'instructor', 'admin') NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    status ENUM('active', 'locked', 'inactive') NOT NULL DEFAULT 'active',
    last_login TIMESTAMP NULL DEFAULT NULL
);

USE auth_db;
ALTER TABLE users_auth 
    ADD COLUMN failed_attempts INT DEFAULT 0;

ALTER TABLE users_auth 
    ADD COLUMN lockout_until DATETIME NULL;
