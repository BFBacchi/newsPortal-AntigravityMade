-- V2__Seed_Data.sql

-- Insert default admin user
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (username, email, password, full_name, enabled, account_non_locked)
VALUES ('admin', 'admin@newsportal.com', '$2a$10$xQXKnXxXxXxXxXxXxXxXxOxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxX', 'Administrator', TRUE, TRUE);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin';

-- Insert default editor user
-- Password: editor123 (BCrypt hashed)
INSERT INTO users (username, email, password, full_name, enabled, account_non_locked)
VALUES ('editor', 'editor@newsportal.com', '$2a$10$xQXKnXxXxXxXxXxXxXxXxOxXxXxXxXxXxXxXxXxXxXxXxXxXxXxXxX', 'Editor User', TRUE, TRUE);

-- Assign EDITOR role to editor user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_EDITOR' FROM users WHERE username = 'editor';

-- Note: These are placeholder passwords. In production, you should:
-- 1. Use proper BCrypt hashed passwords
-- 2. Change default passwords immediately
-- 3. Use environment variables or secure vaults for credentials
