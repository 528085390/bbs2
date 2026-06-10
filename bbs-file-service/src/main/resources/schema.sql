CREATE DATABASE IF NOT EXISTS bbs_file_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bbs_file_db;

CREATE TABLE IF NOT EXISTS file_meta (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_type VARCHAR(64) NOT NULL,
  owner_id BIGINT NOT NULL,
  filename VARCHAR(255) NOT NULL,
  storage_path VARCHAR(512) NOT NULL,
  content_type VARCHAR(128),
  size BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_owner (owner_type, owner_id)
);

