ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE users ALTER COLUMN name DROP NOT NULL;

CREATE INDEX idx_users_provider ON users(provider);
CREATE INDEX idx_users_provider_id ON users(provider, provider_id);

CREATE TABLE oauth_states (
    id UUID PRIMARY KEY,
    state VARCHAR(255) NOT NULL UNIQUE,
    provider VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_oauth_states_state ON oauth_states(state);
CREATE INDEX idx_oauth_states_expires_at ON oauth_states(expires_at);

