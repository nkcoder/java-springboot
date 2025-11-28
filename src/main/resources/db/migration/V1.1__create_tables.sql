-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE IF NOT EXISTS users
(
    id                UUID PRIMARY KEY             DEFAULT uuid_generate_v4(),
    email             VARCHAR(255) UNIQUE NOT NULL,
    password          VARCHAR(255)        NOT NULL,
    name              VARCHAR(255)        NOT NULL,
    role              VARCHAR(50)         NOT NULL DEFAULT 'MEMBER',
    is_email_verified BOOLEAN             NOT NULL DEFAULT FALSE,
    last_login_at     TIMESTAMP,
    created_at        TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id           UUID PRIMARY KEY             DEFAULT uuid_generate_v4(),
    token        VARCHAR(512) UNIQUE NOT NULL,
    token_family VARCHAR(255)        NOT NULL,
    user_id      UUID                NOT NULL,
    expires_at   TIMESTAMP           NOT NULL,
    created_at   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_family ON refresh_tokens (token_family);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);

