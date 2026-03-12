CREATE DATABASE IF NOT EXISTS iot_honeypot;

USE iot_honeypot;

CREATE TABLE IF NOT EXISTS attack_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    protocol VARCHAR(50) NOT NULL,
    source_ip VARCHAR(50) NOT NULL,
    payload TEXT,
    port INT
);
