CREATE DATABASE IF NOT EXISTS bbs_file_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bbs_file_db;

CREATE TABLE IF NOT EXISTS file_meta (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_type VARCHAR(64),
  owner_id BIGINT,
  filename VARCHAR(255) NOT NULL,
  storage_path VARCHAR(500) NOT NULL,
  content_type VARCHAR(100),
  size BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_owner (owner_type, owner_id)
);

