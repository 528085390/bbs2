USE bbs_db;

-- Insert default roles
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_USER', 'Regular user');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_MOD', 'Moderator');
INSERT IGNORE INTO roles (name, description) VALUES ('ROLE_ADMIN', 'Administrator');

-- Create an admin user (replace PASSWORD_HASH with a BCrypt-hashed password)
-- Example: generate a BCrypt hash for 'ChangeMe123' and replace below
-- INSERT INTO users (username, email, password, display_name, enabled) VALUES ('admin', 'admin@example.com', '$2a$10$PASSWORD_HASH', 'Administrator', true);
-- Then assign role:
-- INSERT INTO user_roles (user_id, role_id) VALUES ((SELECT id FROM users WHERE username='admin'), (SELECT id FROM roles WHERE name='ROLE_ADMIN'));

-- Example sections
INSERT IGNORE INTO sections (title, description, order_index, visibility) VALUES ('General', 'General discussion', 1, 'PUBLIC');
INSERT IGNORE INTO sections (title, description, order_index, visibility) VALUES ('Announcements', 'Official announcements', 0, 'PUBLIC');

