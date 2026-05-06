USE bbs_auth_db;

INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MOD'), ('ROLE_ADMIN');

-- 将下面哈希替换成真实 BCrypt 密码哈希
-- INSERT INTO users (username, email, password, display_name) VALUES
-- ('admin', 'admin@example.com', '$2a$10$replace_with_real_bcrypt_hash', 'Administrator');
-- INSERT INTO user_roles (user_id, role_id)
-- VALUES ((SELECT id FROM users WHERE username='admin'), (SELECT id FROM roles WHERE name='ROLE_ADMIN'));

