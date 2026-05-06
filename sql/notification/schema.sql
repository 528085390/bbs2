CREATE DATABASE IF NOT EXISTS bbs_notify_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bbs_notify_db;

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(64) NOT NULL,
  payload JSON,
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_notify_user (user_id, is_read)
);

