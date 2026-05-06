USE bbs_forum_db;

INSERT INTO sections (title, description, order_index, visibility)
VALUES ('General', 'General Discussion', 1, 'PUBLIC')
ON DUPLICATE KEY UPDATE title = VALUES(title);

