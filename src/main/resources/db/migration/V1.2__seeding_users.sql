-- password: Demo12345! for Daniel, and Admin12345! for admin
INSERT INTO users (id, email, password, name, role, is_email_verified, last_login_at, created_at, updated_at)
VALUES (uuid_generate_v4(), 'demo@timor.com', '$2a$12$fPGex4mxJbTzNx3UzOF5puIeumM57uI6q5c8u1urEUTizt8XMZvOC', 'Daniel', 'member', false,
        null, NOW(), NOW()),
       (uuid_generate_v4(), 'admin@timor.com', '$2a$12$j3Mav/owJ/vgztY5VD2YVuRa5MZszAsh1fJKibH2Li1ZS0wV5j5Ci', 'Admin', 'admin', false,
        null, NOW(), NOW());